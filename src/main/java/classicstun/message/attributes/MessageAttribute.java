package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;

/**
 * @author JiangSenwei
 */
public abstract class MessageAttribute {
    private MessageAttributeType type;
    private byte[] value;

    public MessageAttribute(MessageAttributeType type, byte[] value) {
        this.type = type;
        this.value = value;
    }

    public MessageAttributeType getType() {
        return type;
    }

    public byte[] getValue() {
        return value;
    }

}
