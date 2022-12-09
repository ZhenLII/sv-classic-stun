package classicstun.communication.nat.detection;

import classicstun.communication.ResponseEvent;
import classicstun.communication.StunClient;
import classicstun.message.Message;
import classicstun.message.MessageHeader;
import classicstun.message.attributes.*;
import classicstun.message.enums.MessageAttributeType;
import classicstun.message.enums.MessageHeaderType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author JiangZhenli
 */
public class NATTypeDiscovery {

    private StunClient client;
    private SocketAddress serverAddress;

    public NATTypeDiscovery(StunClient client, SocketAddress serverAddress) {
        this.client = client;
        this.serverAddress = serverAddress;
    }

    private Map<MessageAttributeType,MessageAttribute> testI(ChangedAddress changedAddress) {
        Message message1 = new Message(MessageHeader.init(MessageHeaderType.Binding_Request,null,true), List.of());
        SocketAddress target = changedAddress == null ? serverAddress : new InetSocketAddress(changedAddress.getIpAddress(),changedAddress.getPort());
        ResponseEvent event = client.send(client.getChannel1(),message1,target);
        if(event.getResponse() == null) {
            if(event.getErr() != null){
                throw new IllegalStateException("request error: " + event.getErr().getMessage());
            }
            return null;
        } else {
            Optional<MessageAttribute> mappedAddressOptional =  event.getResponse().getMessageAttributes().stream().filter(m -> MessageAttributeType.MAPPED_ADDRESS.equals(m.getType())).findFirst();
            Optional<MessageAttribute> changedAddressOptional =  event.getResponse().getMessageAttributes().stream().filter(m -> MessageAttributeType.CHANGED_ADDRESS.equals(m.getType())).findFirst();
            if(mappedAddressOptional.isEmpty() || changedAddressOptional.isEmpty()) {
                throw new IllegalStateException("Wrong server behavior.The MappedAddress attribute is required");
            } else {
                return Map.of(mappedAddressOptional.get().getType(),mappedAddressOptional.get(),changedAddressOptional.get().getType(),changedAddressOptional.get());
            }
        }
    }

    private MappedAddress testII() {
        return testIIorIII(true);
    }

    private MappedAddress testIII() {
        return testIIorIII(false);
    }

    private MappedAddress testIIorIII(boolean changeIp) {
        ChangeRequest changeRequest = new ChangeRequest(changeIp,true);
        Message message = new Message(MessageHeader.init(MessageHeaderType.Binding_Request,null,true), List.of(changeRequest));
        SocketAddress target = serverAddress;
        ResponseEvent event = client.send(client.getChannel1(),message,target);
        if(event.getResponse() == null) {
            if(event.getErr() != null){
                throw new IllegalStateException("request error: " + event.getErr().getMessage());
            }
            return null;
        } else {
            Optional<MessageAttribute> mappedAddressOptional =  event.getResponse().getMessageAttributes().stream().filter(m -> m instanceof MappedAddress).findFirst();
            if(mappedAddressOptional.isEmpty()) {
                throw new IllegalStateException("Wrong server behavior.The MappedAddress attribute is required");
            } else {
                return (MappedAddress) mappedAddressOptional.get();
            }
        }
    }

    public NATTypeEnum discover() throws IOException {
        InetSocketAddress local = (InetSocketAddress) client.getChannel1().getLocalAddress();
        Map<MessageAttributeType,MessageAttribute> testIResult = testI(null);

        if(testIResult == null) {
            return NATTypeEnum.UDP_Blocked;
        }
        MappedAddress mapI1 = (MappedAddress) testIResult.get(MessageAttributeType.MAPPED_ADDRESS);
        ChangedAddress changedAddress = (ChangedAddress) testIResult.get(MessageAttributeType.CHANGED_ADDRESS);
        boolean ipSame = local.getPort() == mapI1.getPort() && local.getAddress().equals(mapI1.getIpAddress());
        MappedAddress mapII = testII();
        if(ipSame) {
            if(mapII == null) {
                return NATTypeEnum.Symmetric_UDP_Firewall;
            } else {
                return NATTypeEnum.Open_Internet;
            }
        } else {
            if(mapII == null) {
                testIResult = testI(changedAddress);
                if (testIResult == null) {
                    throw new IllegalArgumentException("No response received, this should not happen.");
                }
                MappedAddress mapI2 = (MappedAddress) testIResult.get(MessageAttributeType.MAPPED_ADDRESS);
                ipSame = mapI1.getPort() == mapI2.getPort() && mapI1.getIpAddress().equals(mapI2.getIpAddress());
                if (!ipSame) {
                    return NATTypeEnum.Symmetric_NAT;
                }
                MappedAddress mapIII = testIII();
                if(mapIII == null) {
                    return NATTypeEnum.Port_Restricted_Cone_NAT;
                } else {
                    return NATTypeEnum.Restricted_Cone_NAT;
                }
            }else {
                return NATTypeEnum.Full_Cone_NAT;
            }
        }
    }

}
