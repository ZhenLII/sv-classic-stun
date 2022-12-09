package classicstun.message;

import classicstun.message.attributes.MessageAttribute;
import classicstun.message.attributes.MessageIntegrity;
import classicstun.message.attributes.Username;
import classicstun.message.enums.MessageAttributeType;
import classicstun.message.exception.MessageExcepion;
import common.utils.HMAC_SHA1Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author JiangZhenli
 */
public class Message {
    private MessageHeader header;
    private List<MessageAttribute> messageAttributes;

    public Message(MessageHeader header, List<MessageAttribute> messageAttributes) {
        this.header = header;
        this.messageAttributes = messageAttributes;
    }

    public MessageHeader getHeader() {
        return header;
    }

    public List<MessageAttribute> getMessageAttributes() {
        return messageAttributes;
    }

    public byte[] encode() throws MessageExcepion {
        int length = 0;
        byte[] dataBytes = new byte[length];
        for(MessageAttribute attribute : messageAttributes) {
            byte[] attributeData = attribute.encode();
            length += attributeData.length;
            byte[] newData = new byte[length];
            System.arraycopy(dataBytes,0,newData,0,dataBytes.length);
            System.arraycopy(attributeData,0,newData,dataBytes.length, attributeData.length);
            dataBytes = newData;
        }
        header.setMessageLength(length);
        byte[] headerBytes = header.encode();
        byte[] messageBytes = new byte[headerBytes.length + dataBytes.length];
        System.arraycopy(headerBytes,0, messageBytes,0,headerBytes.length);
        System.arraycopy(dataBytes,0, messageBytes,headerBytes.length, dataBytes.length);
        return messageBytes;
    }

    public byte[] encodeWithIntegrity(String username,String password,boolean addUsername) throws MessageExcepion {
        if(username == null) {
            addUsername = false;
        }
        if(getMessageAttributes() != null) {
            Map<MessageAttributeType,MessageAttribute> map = new HashMap<>();
            for(MessageAttribute attribute : getMessageAttributes()) {
                if(map.containsKey(attribute.getType())) {
                    throw new MessageExcepion("Multiple attribute type");
                }
                map.put(attribute.getType(),attribute);
            }
            if(!map.containsKey(MessageAttributeType.MESSAGE_INTEGRITY)) {
                if(password == null) {
                    throw new IllegalArgumentException("password is null");
                }
                if(map.containsKey(MessageAttributeType.USERNAME)) {
                    MessageAttribute attribute = map.get(MessageAttributeType.USERNAME);
                    if(attribute instanceof Username) {
                        if(!Objects.equals(((Username) attribute).getUsername(),username)) {
                            throw new MessageExcepion("Username is different with the specified one");
                        } else {
                            addUsername = false;
                        }
                    } else {
                        throw new MessageExcepion("Wrong attribute with type USERNAME");
                    }
                }
                if(addUsername) {
                    Username usernameAttribute = new Username(username);
                    getMessageAttributes().add(usernameAttribute);
                }

                byte[] raw = encode();
                byte[] hmac = HMAC_SHA1Utils.hmac(raw,password);
                if(hmac == null) {
                    throw new MessageExcepion("Build integrity error");
                }
                byte[] hmacAttributeBytes = new MessageIntegrity(hmac).encode();
                byte[] data = new byte[raw.length + hmacAttributeBytes.length];

                System.arraycopy(raw,0,data,0,raw.length);
                System.arraycopy(hmacAttributeBytes,0,data,raw.length,hmacAttributeBytes.length);
                return data;
            } else {
                return encode();
            }
        } else {
            throw new MessageExcepion("MessageAttribute is null.");
        }

    }

    public static Message parse(byte[] data) throws MessageExcepion {
        MessageHeader header = MessageHeader.decode(data);
        int length = header.getMessageLength();
        if(length != data.length - 20) {
            throw new MessageExcepion("Wrong length");
        }
        byte[] attributeData = new byte[data.length - 20];
        System.arraycopy(data,20,attributeData,0,length);
        List<MessageAttribute> messageAttributes = MessageAttribute.parse(attributeData);

        return new Message(header,messageAttributes);
    }
}
