package classicstun.message;

import classicstun.message.attributes.MessageAttribute;
import classicstun.message.enums.MessageHeaderType;
import classicstun.message.exception.MessageExcepion;

import java.awt.*;
import java.util.List;

/**
 * @author JiangZhenli
 */
public class Message {
    private MessageHeader header;
    private List<MessageAttribute> messageAttributes;
    private byte[] bytes;


    public Message(MessageHeader header, List<MessageAttribute> messageAttributes) throws MessageExcepion {
        this.header = header;
        this.messageAttributes = messageAttributes;
        bytes = encode();
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
            System.arraycopy(attributeData,0,newData,dataBytes.length - 1 ,attributeData.length);
            dataBytes = newData;
        }
        header.setMessageLength(length);
        byte[] headerBytes = header.encode();
        byte[] messageBytes = new byte[headerBytes.length + dataBytes.length];
        System.arraycopy(headerBytes,0, messageBytes,0,headerBytes.length);
        System.arraycopy(dataBytes,0, messageBytes,headerBytes.length - 1, dataBytes.length);
        return messageBytes;
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
