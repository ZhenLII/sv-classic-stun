package classicstun.communication;

import classicstun.message.Message;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.EventObject;

/**
 * @author JiangZhenli
 */
public class ResponseEvent extends EventObject {

    // 在哪个地址端口上接收到该响应的
    private SocketAddress receive;

    // 响应是从哪个地址端口上发出的（来源地址和端口）
    private SocketAddress from;

    // 原始请求报文
    private byte[] rawRequest;

    // 解析后的响应报文
    private Message response;

    // 原始响应报文
    private byte[] rawResponse;

    // 可能存在的错误
    private Exception err;
    /**
     * Constructs a prototypical Event.
     *
     * @param source the object on which the Event initially occurred
     * @throws IllegalArgumentException if source is null
     */
    ResponseEvent(Object source, SocketAddress receive, SocketAddress from, byte[] rawRequest, Message response,byte[] rawResponse, Exception err) {
        super(source);
        this.receive = receive;
        this.from = from;
        this.rawRequest = rawRequest;
        this.response = response;
        this.rawResponse = rawResponse;
        this.err = err;
    }

    public SocketAddress getReceive() {
        return receive;
    }

    public SocketAddress getFrom() {
        return from;
    }

    public byte[] getRawRequest() {
        return rawRequest;
    }

    public Message getResponse() {
        return response;
    }

    public byte[] getRawResponse() {
        return rawResponse;
    }

    public Exception getErr() {
        return err;
    }

    @Override
    public String toString() {
        if(rawRequest == null) {
            return "Non request request";
        }
        try {
            Message request = Message.parse(rawRequest);
            String transactionId = new ClientMessageHandle(request.getHeader().getTransactionId()).getHexTransactionId();
            Boolean isResponse = response != null;
            String receiveAt = receive == null ? "null" : ((InetSocketAddress) receive).getAddress().toString() + ":" + ((InetSocketAddress) receive).getPort();
            String receiveFrom = from == null ? "null" : ((InetSocketAddress) from).getAddress().toString() + ":" + ((InetSocketAddress) from).getPort();
            String err = this.err == null ? "null" : this.err.getMessage();

            return "\nResponseEvent{\n" +
                    " isResponse: " + isResponse + ", \n" +
                    " transactionId: " + transactionId + ", \n" +
                    " receiveAt: " + receiveAt + ", \n" +
                    " receiveFrom: " + receiveFrom + ", \n" +
                    " err: " + err + "\n" +
                    "}";
        } catch (Exception e) {
            return "Unknown request response";
        }

    }
}
