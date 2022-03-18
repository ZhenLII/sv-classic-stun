package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author JiangZhenli
 */
public class UnknownAttribute extends MessageAttribute {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    UnknownAttribute() {
        super(MessageAttributeType.UNKNOWN_ATTRIBUTES);
    }

    @Override
    public byte[] encode() {
        return new byte[0];
    }

    @Override
    public void decode(byte[] attributeData) {

    }
}
