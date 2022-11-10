package classicstun.message;

import classicstun.message.attributes.MessageAttribute;
import classicstun.message.exception.MessageExcepion;

import java.util.List;

/**
 * @author JiangZhenli
 */
public class Message {
    MessageHeader header;
    List<MessageAttribute> messageAttributes;

    public Message(MessageHeader header, List<MessageAttribute> messageAttributes) {
        this.header = header;
        this.messageAttributes = messageAttributes;
    }

    public MessageHeader getHeader() {
        return header;
    }

    public void setHeader(MessageHeader header) {
        this.header = header;
    }

    public List<MessageAttribute> getMessageAttributes() {
        return messageAttributes;
    }

    public void setMessageAttributes(List<MessageAttribute> messageAttributes) {
        this.messageAttributes = messageAttributes;
    }

    static Message parse(byte[] data) throws MessageExcepion {
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
