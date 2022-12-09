package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author JiangZhenli
 */
public class UnknownAttributes extends MessageAttribute {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private byte[] unknownData;
    UnknownAttributes() {
        super(MessageAttributeType.UNKNOWN_ATTRIBUTES);
    }

    @Override
    public byte[] encodeValue() {
        return unknownData;
    }

    @Override
    public void decode(byte[] attributeData) {
        this.unknownData = attributeData;
    }
}
