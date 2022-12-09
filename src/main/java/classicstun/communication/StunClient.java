package classicstun.communication;

import classicstun.message.Message;
import classicstun.message.attributes.MessageAttribute;
import classicstun.message.attributes.MessageIntegrity;
import classicstun.message.attributes.Username;
import classicstun.message.enums.MessageAttributeType;
import classicstun.message.enums.MessageHeaderType;
import classicstun.message.exception.MessageExcepion;
import common.utils.HMAC_SHA1Utils;
import communication.udp.DatagramAcceptor;
import communication.udp.DatagramMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author JiangZhenli
 */
public class StunClient extends DatagramAcceptor {
    private Logger log = LoggerFactory.getLogger(getClass());
    private Map<ClientMessageHandle,PendingRequest> pendingRequests = new ConcurrentHashMap<>();
    private long timeout = 10000;
    private DatagramChannel channel1;
    private DatagramChannel channel2;
    private Map<String,String> users = new ConcurrentHashMap<>();
    public StunClient(Inet4Address addr1, int port1, Inet4Address addr2, int port2) throws IOException {
        super();
        if(addr1.equals(addr2) && port1 == port2) {
            throw new IllegalArgumentException("Same address and port");
        }

        channel1 = DatagramChannel.open();
        channel1.configureBlocking(false);
        SocketAddress socketAddress1 = new InetSocketAddress(addr1,port1);
        channel1.bind(socketAddress1);
        channel1.register(selector, SelectionKey.OP_READ);

        channel2 = DatagramChannel.open();
        channel2.configureBlocking(false);
        SocketAddress socketAddress2 = new InetSocketAddress(addr2,port2);
        channel2.bind(socketAddress2);
        channel2.register(selector, SelectionKey.OP_READ);

        addHandler(new DatagramMessageHandler() {
            @Override
            public boolean decode(DatagramHandleContext context) {
                try {
                    Message message = Message.parse(context.getData());
                    context.attach(message);
                    return true;
                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                    return false;
                }
            }

            @Override
            public void handle(DatagramHandleContext context) {
                handleResponse(context);
            }
        });
    }

    public StunClient(Inet4Address address, int port1, int port2) throws IOException {
        this(address,port1,address,port2);
    }

    public DatagramChannel getChannel1() {
        return channel1;
    }

    public DatagramChannel getChannel2() {
        return channel2;
    }

    /**
     * 同步请求
     * 不处理响应中的任何错误
     * */
    public ResponseEvent send(DatagramChannel channel, Message message, SocketAddress target,String username) {
        checkRequest(message);
        if(username !=null && users.get(username) == null) {
            throw new IllegalArgumentException("User is not exist");
        }
        byte[] data;
        try {
            if(username != null) {
                data = message.encodeWithIntegrity(username,users.get(username),true);
            } else {
                data = message.encode();
            }
        } catch (MessageExcepion e) {
            throw new IllegalArgumentException("Message encode error: " + e.getMessage());
        }
        final SyncResponseListener syncResponse = new SyncResponseListener();
        synchronized (syncResponse){
            PendingRequest pendingRequest = new PendingRequest(channel,data,target,syncResponse);
            ClientMessageHandle handle = new ClientMessageHandle(message.getHeader().getTransactionId());
            pendingRequest.assigned(handle);
           try {
               channel.send(ByteBuffer.wrap(data),target);
           } catch (Exception e) {
               log.error(e.getMessage(),e);
               pendingRequest.cancel();
           }

            long stopTime = System.nanoTime() + timeout * 1000000;
            try {
                while (syncResponse.getResponse() == null && System.nanoTime() < stopTime) {
                    syncResponse.wait(timeout);
                }
            } catch (InterruptedException e) {
                log.error(e.getMessage(),e);
                pendingRequest.cancel();
            }
            if (syncResponse.getResponse() == null) {
                syncResponse.response =
                        new ResponseEvent(StunClient.this, null,null, data, null,null, null);
            }
            checkResponse(syncResponse.response);
            return syncResponse.response;
        }
    }

    public ResponseEvent send(DatagramChannel channel, Message message, SocketAddress target){
        return send(channel,message,target,null);
    }

    private void checkRequest(Message message) {
        assert message != null;
        assert message.getHeader() != null;
        assert message.getMessageAttributes() != null;

        assert message.getHeader().getMessageType() != null;
        assert message.getHeader().getTransactionId() != null;

        if(!MessageHeaderType.Binding_Request.equals(message.getHeader().getMessageType())) {
            throw new IllegalStateException("Message is not request message");
        }
        Map<MessageAttributeType,MessageAttribute> attributeMap = new HashMap<>();
        for(MessageAttribute attribute : message.getMessageAttributes()) {
            if(attributeMap.containsKey(attribute.getType())) {
                throw new IllegalStateException("Duplicated attribute type in request");
            } else {
                attributeMap.put(attribute.getType(),attribute);
            }
        }
        boolean isNeedIntegrity = message.getMessageAttributes().stream().anyMatch(a -> a.getType().equals(MessageAttributeType.USERNAME));

        if(isNeedIntegrity && attributeMap.get(MessageAttributeType.MESSAGE_INTEGRITY) == null) {
            throw new IllegalStateException("Request need integrity parameter");
        }

    }


    /**
     * 检验响应报文的合法性
     * */
    private void checkResponse(ResponseEvent responseEvent){
        if(responseEvent != null && responseEvent.getResponse() != null) {
            Message request;
            try {
               request = Message.parse(responseEvent.getRawRequest());
            } catch (MessageExcepion e) {
                log.error(e.getMessage(),e);
                throw new IllegalStateException("Request data parse error.");
            }

            Message response = responseEvent.getResponse();
            List<MessageAttribute> requestAttributes = request.getMessageAttributes();
            Map<MessageAttributeType,MessageAttribute> requestAttributeMap = new HashMap<>();
            for(MessageAttribute attribute : requestAttributes) {
                requestAttributeMap.put(attribute.getType(),attribute);
            }
            List<MessageAttribute> responseAttributes = response.getMessageAttributes();
            Map<MessageAttributeType,MessageAttribute> responseAttributeMap = new HashMap<>();
            for(MessageAttribute attribute : responseAttributes) {
                if(responseAttributeMap.containsKey(attribute.getType())) {
                    throw new IllegalStateException("Duplicated attribute type in response");
                } else {
                    responseAttributeMap.put(attribute.getType(),attribute);
                }
            }
            boolean isCheckIntegrity = responseAttributeMap.get(MessageAttributeType.MESSAGE_INTEGRITY) != null;

            if(isCheckIntegrity) {
                Username username = (Username) requestAttributeMap.get(MessageAttributeType.USERNAME);
                if(username == null || users.get(username.getUsername()) == null) {
                    throw new IllegalStateException("Not find the request user");
                }else {
                    MessageIntegrity messageIntegrity = (MessageIntegrity) responseAttributeMap.get(MessageAttributeType.MESSAGE_INTEGRITY);
                    String sharedKey = users.get(username.getUsername());
                    byte[] raw = responseEvent.getRawResponse();
                    byte[] plainData = Arrays.copyOfRange(raw,raw.length - 24, raw.length);
                    if(!HMAC_SHA1Utils.validate(plainData,messageIntegrity.getHmac(),sharedKey)) {
                        throw new IllegalStateException("Validation Integrity Error.");
                    }
                }
            }
        }

    }


    public synchronized void stop() {
        closed = true;
        try {
            channel1.close();
            channel2.close();
            selector.close();
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }


    private void handleResponse(DatagramMessageHandler.DatagramHandleContext context) {
        InetSocketAddress origin = ((InetSocketAddress)(context.getOrigin()));
        Message message;
        if(context.attachment() instanceof Message) {
            message = (Message) context.attachment();
        } else {
            try {
                message = Message.parse(context.getData());
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                return;
            }
        }
        MessageHeaderType type = message.getHeader().getMessageType();
        if(MessageHeaderType.Binding_Response.equals(type) || MessageHeaderType.Binding_Error_Response.equals(type)) {
            ClientMessageHandle handle = new ClientMessageHandle(message.getHeader().getTransactionId());
            PendingRequest pendingRequest = pendingRequests.get(handle);
            if(pendingRequest != null) {
                if(!pendingRequest.isCancel() && !pendingRequest.isResponded()) {
                    pendingRequest.receiveResponse();
                    ResponseEvent responseEvent = new ResponseEvent(this,context.getReceive(),origin,pendingRequest.sentMessage,message,context.getData(),null);
                    pendingRequest.listener.onResponse(responseEvent);
                }
            } else {
                log.warn("Not find matched request: {}",new ClientMessageHandle(message.getHeader().getTransactionId()).getHexTransactionId());
            }
        }
    }

    /**
     *
     * 作为一个未确定结果的进行中的请求，进行请求重试
     * 请求响应、超时后就是一个确定结果的请求了
     */
    class PendingRequest implements Runnable {
        private ClientMessageHandle key;
        private DatagramChannel sendChannel;
        private byte[] sentMessage;
        private SocketAddress target;
        private int retryTimes = 1;
        private long interval = 100;
        private boolean responded = false;
        private boolean cancel = false;
        private ResponseListener listener;
        private PendingRequest(DatagramChannel sendChannel,
                               byte[] sentMessage,
                               SocketAddress target,
                               ResponseListener listener) {
            this.sendChannel = sendChannel;
            this.sentMessage = sentMessage;
            this.target = target;
            this.listener = listener;
        }

        public synchronized void assigned(ClientMessageHandle handle){
            if(key == null) {
                key = handle;
                pendingRequests.put(handle,this);
                CompletableFuture.runAsync(this);
            }
        }

        public synchronized void receiveResponse(){
            this.responded = true;
        }

        public synchronized void cancel(){
            this.cancel = true;
        }

        // 重试任务
        @Override
        public void run() {
            try {
                log.info("Run PendingRequest Key : {}",key.getHexTransactionId());
                Thread.sleep(interval);
                interval *= 2;
                while (!responded && retryTimes < 9 && !cancel) {
                    synchronized (this){
                            log.info("PendingRequest Retry Key :{}, interval: {}",key.getHexTransactionId(),interval);
                            sendChannel.send(ByteBuffer.wrap(sentMessage),target);
                            wait(interval);
                            retryTimes++;
                            interval = interval != 1600 ? interval * 2 : interval;
                    }
                }
                if(!responded) {
                    listener.onResponse(new ResponseEvent(StunClient.this,null,null,sentMessage,null,null,null));
                }
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                if(!responded){
                    listener.onResponse(new ResponseEvent(StunClient.this,null,null,sentMessage,null,null, e));
                }
            } finally {
                if(key != null) {
                    log.debug("remove request: {}",key.getHexTransactionId());
                    pendingRequests.remove(key);
                }
            }
        }

        public boolean isResponded() {
            return responded;
        }

        public boolean isCancel() {
            return cancel;
        }
    }

    static class SyncResponseListener implements ResponseListener {

        private ResponseEvent response = null;

        public synchronized void onResponse(ResponseEvent event) {
            this.response = event;
            this.notify();
        }

        public ResponseEvent getResponse() {
            return response;
        }
    }
}
