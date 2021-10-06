package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author JiangSenwei
 */
public class Password extends MessageAttribute {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    Password() {
        super(MessageAttributeType.PASSWORD);
    }

    @Override
    public byte[] encode() {
        return new byte[0];
    }

    @Override
    public void decode(byte[] attributeData) {

    }
}
