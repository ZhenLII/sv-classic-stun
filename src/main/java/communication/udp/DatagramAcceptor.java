package communication.udp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author JiangZhenli
 */
public class DatagramAcceptor implements Runnable {

    private Logger log = LoggerFactory.getLogger(getClass());

    private List<DatagramMessageHandler> handlers = new ArrayList<>();

    private final static ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(16);

    protected boolean closed = true;

    protected Selector selector;

    public DatagramAcceptor() throws IOException {
        selector = Selector.open();
    }

    public void addHandler(DatagramMessageHandler handler) {
        if(handler != null) {
            if(!handlers.contains(handler)) {
                handlers.add(handler);
            }
        }
    }

    public synchronized void start(boolean block) {
        closed = false;
        if(!block) {
            CompletableFuture.runAsync(this,EXECUTOR_SERVICE);
        } else {
            run();
        }
    }

    public synchronized void stop() {
        closed = true;
        try {
            selector.close();
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }

    @Override
    public void run() {
        while (!closed) {
            try {
                if (selector.select() > 0) {
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> it = selectedKeys.iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        it.remove();
                        if (key.isValid() && key.channel() instanceof DatagramChannel && key.isReadable()) {
                            DatagramChannel channel = (DatagramChannel) key.channel();
                            ByteBuffer byteBuffer = ByteBuffer.allocate(1472); // 最大处理 1472 byte UDP 数据部分
                            SocketAddress sourceAddress = channel.receive(byteBuffer);
                            SocketAddress receivedAddress = channel.getLocalAddress();
                            byte[] data = new byte[byteBuffer.position()];
                            System.arraycopy(byteBuffer.array(), 0, data, 0, byteBuffer.position());
                            DatagramMessageHandler.DatagramHandleContext context = new DatagramMessageHandler.DatagramHandleContext(channel, receivedAddress,sourceAddress, data, this);
                            CompletableFuture.runAsync(() -> {
                                // 调用一个匹配的handler处理报文
                                try {
                                    for (DatagramMessageHandler handler : handlers) {
                                        if (handler.decode(context)) {
                                            handler.handle(context);
                                        }
                                    }
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                }
                            }, EXECUTOR_SERVICE);
                        }
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(),e);
            }
        }
    }
}
