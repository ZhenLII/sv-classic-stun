package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;

/**
 * @author JiangSenwei
 */
public class MappedAddress extends MessageAttribute {
    public MappedAddress(MessageAttributeType type, byte[] value) {
        super(type, value);
    }
}
