package classicstun.communication;

import classicstun.message.Message;
import classicstun.message.MessageHeader;
import classicstun.message.attributes.*;
import classicstun.message.enums.ErrorCodeEnum;
import classicstun.message.enums.MessageAttributeType;
import classicstun.message.enums.MessageHeaderType;
import common.utils.HMAC_SHA1Utils;
import communication.udp.DatagramAcceptor;
import communication.udp.DatagramMessageHandler;
import config.LocalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author JiangZhenli
 */
public class StunServer extends DatagramAcceptor implements DatagramMessageHandler {
    private Logger log = LoggerFactory.getLogger(getClass());

    private DatagramChannel stunDaDpChannel;
    private DatagramChannel stunDaCpChannel;
    private DatagramChannel stunCaDpChannel;
    private DatagramChannel stunCaCpChannel;

    private List<DatagramChannel> indexChannelList;

    private ChangedAddress changedAddress;

    private String privateKey1 = LocalConfiguration.get(LocalConfiguration.ConfigEnum.STUN_USERNAME_KEY);
    private String privateKey2 = LocalConfiguration.get(LocalConfiguration.ConfigEnum.STUN_PASSWORD_KEY);

    public StunServer(Inet4Address da,int dp, Inet4Address ca, int cp) throws IOException {
        if(da.equals(ca) || dp == cp) {
            throw new IllegalArgumentException("Same Da and Ca or Same Dp and Cp.");
        }

        changedAddress = new ChangedAddress(cp,ca);
        // 只在默认地址和端口监听请求
        stunDaDpChannel = DatagramChannel.open();
        stunDaDpChannel.configureBlocking(false);
        SocketAddress defaultAddress = new InetSocketAddress(da,dp);
        stunDaDpChannel.bind(defaultAddress);
        stunDaDpChannel.register(selector, SelectionKey.OP_READ);


        // 剩下的Channel只用于响应
        SocketAddress dacpAddress = new InetSocketAddress(da,cp);
        stunDaCpChannel = DatagramChannel.open();
        stunDaCpChannel.bind(dacpAddress);

        SocketAddress cadpAddress = new InetSocketAddress(ca,dp);
        stunCaDpChannel = DatagramChannel.open();
        stunCaDpChannel.bind(cadpAddress);

        SocketAddress cacpAddress = new InetSocketAddress(ca,cp);
        stunCaCpChannel = DatagramChannel.open();
        stunCaCpChannel.bind(cacpAddress);
        stunCaCpChannel.configureBlocking(false);
        stunCaCpChannel.register(selector, SelectionKey.OP_READ);
        addHandler(this);
        indexChannelList = List.of(stunDaDpChannel,stunDaCpChannel,stunCaDpChannel,stunCaCpChannel);
    }

    public synchronized void stop() {
        closed = true;
        try {
            stunDaDpChannel.close();
            stunDaCpChannel.close();
            stunCaDpChannel.close();;
            stunCaCpChannel.close();
            selector.close();
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }


    @Override
    public boolean decode(DatagramHandleContext context) {
        try {
            Message message = Message.parse(context.getData());
            context.attach(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void handle(DatagramHandleContext context) {
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

        try {
            byte[] rawData = context.getData();
            MessageHeader header = message.getHeader();
            List<MessageAttribute> attributeList = message.getMessageAttributes();

            if(!MessageHeaderType.Binding_Request.equals(header.getMessageType())) {
                return;
            }
            String sharedKey = null;
            if(attributeList != null && !attributeList.isEmpty()) {
                int attributeNumber = attributeList.size();
                MessageAttribute last = attributeList.get(attributeNumber - 1);
                if(last instanceof MessageIntegrity) {
                    Username username = null;
                    MessageIntegrity integrity = (MessageIntegrity) last;
                    for(MessageAttribute attribute : attributeList) {
                        if(attribute instanceof Username){
                            username = (Username) attribute;
                        }
                    }
                    ErrorCodeEnum err = null;
                    if(username == null) {
                        err = ErrorCodeEnum.MissingUsername;
                    } else {
                        String usernameStr = username.getUsername();
                        if(!NoStatusUsername.isNoStatusUsername(usernameStr)) {
                            err = ErrorCodeEnum.StaleCredentials;
                        } else {
                            try {
                                NoStatusUsername noStatusUsername = NoStatusUsername.parse(usernameStr, privateKey1);
                                if(noStatusUsername.expired()) {
                                    err = ErrorCodeEnum.StaleCredentials;
                                } else {
                                    sharedKey = noStatusUsername.calculatePassword(privateKey2);
                                    int integrityLength = integrity.encode().length;
                                    byte[] plain = new byte[rawData.length - integrityLength];
                                    System.arraycopy(rawData,0,plain,0,rawData.length - integrityLength);
                                    if(!HMAC_SHA1Utils.validate(plain,integrity.encodeValue(),sharedKey)) {
                                        err =  ErrorCodeEnum.IntegrityCheckFailure;
                                    }
                                }
                            } catch (Exception e) {
                                err = ErrorCodeEnum.StaleCredentials;
                            }
                        }
                    }
                    if(err != null) {
                        errorResponse(context.getChannel(),origin,message,err);
                        return;
                    }
                }
            }

            // 视为无需认证或认证通过，如果进行到这一步，无视请求中的MessageIntegrity和Username属性
            Map<MessageAttributeType,MessageAttribute> attributeMap = new HashMap<>();
            if(attributeList != null) {
                for(MessageAttribute attribute : attributeList) {
                    if(attributeMap.containsKey(attribute.getType())) {
                        errorResponse(context.getChannel(),origin,message,ErrorCodeEnum.BadRequest);
                        return;
                    }
                    attributeMap.put(attribute.getType(),attribute);
                }
            }

            if(attributeMap.containsKey(MessageAttributeType.UNKNOWN_ATTRIBUTES)) {
                errorResponse(context.getChannel(),origin,message,ErrorCodeEnum.UnknownAttribute, new ArrayList<>(){{ add(attributeMap.get(MessageAttributeType.UNKNOWN_ATTRIBUTES));}});
                return;
            }

            List<MessageAttribute> responseAttributes = new ArrayList<>();

            DatagramChannel responseChannel = context.getChannel();

            SocketAddress target = origin;

            MappedAddress mappedAddress = new MappedAddress(origin.getPort(),(Inet4Address) origin.getAddress());

            if(attributeMap.containsKey(MessageAttributeType.CHANGE_REQUEST)) {
                ChangeRequest changeRequest = (ChangeRequest) attributeMap.get(MessageAttributeType.CHANGE_REQUEST);
                responseChannel = indexChannelList.get(changeRequest.truthValue() / 2);
            }

            if(attributeMap.containsKey(MessageAttributeType.RESPONSE_ADDRESS)) {
                ResponseAddress responseAddress = (ResponseAddress) attributeMap.get(MessageAttributeType.RESPONSE_ADDRESS);
                target = new InetSocketAddress(responseAddress.getIpAddress(),responseAddress.getPort());
                ReflectedFrom reflectedFrom = new ReflectedFrom(origin.getPort(),(Inet4Address) origin.getAddress());
                responseAttributes.add(reflectedFrom);
            }

            InetSocketAddress responseChannelLocalAddress = (InetSocketAddress)responseChannel.getLocalAddress();
            SourceAddress sourceAddress = new SourceAddress(responseChannelLocalAddress.getPort(),(Inet4Address) responseChannelLocalAddress.getAddress());

            responseAttributes.add(changedAddress);
            responseAttributes.add(mappedAddress);
            responseAttributes.add(sourceAddress);

            response(responseChannel,target,MessageHeaderType.Binding_Response,message,responseAttributes,sharedKey);

        } catch (Exception e) {
            errorResponse(context.getChannel(),origin,message,ErrorCodeEnum.ServerError);
        }

    }

    private void response(DatagramChannel channel,
                          SocketAddress targetAddress,
                          MessageHeaderType responseType,
                          Message requestMessage,
                          List<MessageAttribute> attributes,
                          String sharedKey

    ) {
        if(MessageHeaderType.Binding_Response.equals(responseType) || MessageHeaderType.Binding_Error_Response.equals(responseType)) {
           try {
               MessageHeader reqHeader = requestMessage.getHeader();
               byte[] transactionId = reqHeader.getTransactionId();
               MessageHeader header = MessageHeader.init(responseType,transactionId,false);
               Message message = new Message(header,attributes);
               byte[] data = sharedKey == null ? message.encode() : message.encodeWithIntegrity(null,sharedKey,false);
               if (channel.isOpen()) {
                   ByteBuffer buffer = ByteBuffer.wrap(data);
                   channel.send(buffer,targetAddress == null ? channel.getRemoteAddress() : targetAddress);
               } else {
                   log.warn("Channel was closed already.");
               }
           } catch (Exception e) {
               log.error(e.getMessage(),e);
           }
        }
    }

    private void errorResponse(DatagramChannel channel, SocketAddress target, Message requestMessage, ErrorCodeEnum code,List<MessageAttribute> attributes){
        ErrorCode errorCode = new ErrorCode(code);
        List<MessageAttribute> noeAttributes = new ArrayList<>(){{
            add(errorCode);
        }};
        if(attributes != null) {
            noeAttributes.addAll(attributes);
        }
        response(channel,target,MessageHeaderType.Binding_Error_Response,requestMessage,noeAttributes,null);
    }

    private void errorResponse(DatagramChannel channel,SocketAddress target, Message requestMessage, ErrorCodeEnum code) {
        ErrorCode errorCode = new ErrorCode(code);
        List<MessageAttribute> noeAttributes = new ArrayList<>(){{
            add(errorCode);
        }};
        errorResponse(channel,target,requestMessage,code,noeAttributes);
    }
}
