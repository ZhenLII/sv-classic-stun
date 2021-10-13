package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author JiangZhenli
 *
 * 0                   1                   2                   3
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                   0                     |Class|     Number    |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |      Reason Phrase (variable)                                ..
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */
public class ErrorCode extends MessageAttribute {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    ErrorCode() {
        super(MessageAttributeType.ERROR_CODE);
    }

    @Override
    public byte[] encode() {
        return new byte[0];
    }

    @Override
    public void decode(byte[] attributeData) {

    }
}
