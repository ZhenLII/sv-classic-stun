package communication.udp;

import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author JiangZhenli
 */
public interface DatagramMessageHandler {

    boolean decode(DatagramHandleContext context);

    void handle(DatagramHandleContext context);

    class DatagramHandleContext {
        private DatagramChannel channel;
        private SocketAddress receive;
        private SocketAddress origin;
        private byte[] data;
        private DatagramAcceptor acceptor;

        private volatile Object attachment;

        private static final AtomicReferenceFieldUpdater<DatagramHandleContext,Object>
                attachmentUpdater = AtomicReferenceFieldUpdater.newUpdater(
                DatagramHandleContext.class, Object.class, "attachment"
        );


        public final Object attach(Object ob) {
            return attachmentUpdater.getAndSet(this, ob);
        }

        public final Object attachment() {
            return attachment;
        }

        public DatagramHandleContext(DatagramChannel channel,SocketAddress receive, SocketAddress origin, byte[] data, DatagramAcceptor acceptor) {
            this.channel = channel;
            this.receive = receive;
            this.origin = origin;
            this.data = data;
            this.acceptor = acceptor;
        }

        public DatagramChannel getChannel() {
            return channel;
        }

        public SocketAddress getReceive() {
            return receive;
        }

        public SocketAddress getOrigin() {
            return origin;
        }

        public byte[] getData() {
            return data;
        }

        public DatagramAcceptor getAcceptor() {
            return acceptor;
        }

    }
}
