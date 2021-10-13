package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;
import classicstun.message.exception.MessageAttributeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author JiangZhenli
 */
public class Username extends MessageAttribute {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    Username() {
        super(MessageAttributeType.USERNAME);
    }

    private String username;

    @Override
    public byte[] encode() {
        return new byte[0];
    }

    @Override
    public void decode(byte[] attributeData) throws MessageAttributeException {
        if(attributeData.length <= 0 || attributeData.length % 4 != 0) {
            throw new MessageAttributeException("Username Length Error.");
        }

        this.username = new String(attributeData);
        this.setValue(attributeData);
    }
}
