package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author JiangZhenli
 */
public class MessageIntegrity extends MessageAttribute {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private byte[] hmac;

    MessageIntegrity() {
        super(MessageAttributeType.MESSAGE_INTEGRITY);
    }

    public MessageIntegrity(byte[] hmac) {
        super(MessageAttributeType.MESSAGE_INTEGRITY);
        this.hmac = hmac;
    }



    @Override
    public byte[] encodeValue() {
        return hmac;
    }

    @Override
    public void decode(byte[] attributeData) {
        this.hmac = attributeData;
    }

    public byte[] getHmac() {
        return hmac;
    }
}
