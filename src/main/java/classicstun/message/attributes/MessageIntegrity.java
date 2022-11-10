package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author JiangZhenli
 */
public class MessageIntegrity extends MessageAttribute {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private byte[] HMAC_SHA1;

    MessageIntegrity() {
        super(MessageAttributeType.MESSAGE_INTEGRITY);
    }

    @Override
    public byte[] encode() {
        return HMAC_SHA1;
    }

    @Override
    public void decode(byte[] attributeData) {
        this.HMAC_SHA1 = attributeData;
    }
}
