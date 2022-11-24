package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author JiangZhenli
 */
public class UnknownAttribute extends MessageAttribute {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private byte[] unknownType;
    UnknownAttribute() {
        super(MessageAttributeType.UNKNOWN_ATTRIBUTES);
    }

    @Override
    public byte[] encodeValue() {
        return unknownType;
    }

    @Override
    public void decode(byte[] attributeData) {
        this.unknownType = attributeData;
    }
}
