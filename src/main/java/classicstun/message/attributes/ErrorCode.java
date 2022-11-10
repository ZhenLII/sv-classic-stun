package classicstun.message.attributes;

import classicstun.message.enums.ErrorCodeEnum;
import classicstun.message.enums.MessageAttributeType;
import classicstun.message.exception.MessageAttributeException;
import common.exception.ByteUiltsException;
import common.utils.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

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

    private ErrorCodeEnum errorCode;
    String reasonPhrase;

    public ErrorCode(ErrorCodeEnum errorCode) {
        super(MessageAttributeType.ERROR_CODE);
        this.errorCode = errorCode;
        this.reasonPhrase = errorCode.getReason();
    }

    @Override
    public byte[] encode() {
        byte[] classNumberBytes = new byte[4];
        if(this.errorCode != null) {
            int code = this.errorCode.getCode();
            int hundredsDigit = code / 100;
            int number = code % (hundredsDigit * 100);
            System.arraycopy(ByteUtils.intToByteArray(hundredsDigit),1,classNumberBytes,0,3);
            System.arraycopy(ByteUtils.intToByteArray(number),3,classNumberBytes,3,1);
        }
        return new byte[0];
    }

    @Override
    public void decode(byte[] attributeData) throws MessageAttributeException {
        if(attributeData.length % 4 != 0 && attributeData.length < 4) {
            throw new MessageAttributeException("ErrorCode Attribute Length Error.");
        }
        // parse class and number
        byte[] hundredsDigitByte = new byte[]{0,0,0};
        System.arraycopy(attributeData,0,hundredsDigitByte,0,3);
        if(hundredsDigitByte[0] != 0 || hundredsDigitByte[1] != 0 || hundredsDigitByte[2]  > 6) {
            throw new MessageAttributeException("ErrorCode Attribute Class Error.");
        }
        int hundredsDigit = hundredsDigitByte[2];
        int number = attributeData[3];
        if(number > 99) {
            throw new MessageAttributeException("ErrorCode Attribute Number Error.");
        }
        int code = hundredsDigit * 100 + number;
        ErrorCodeEnum errorCode = ErrorCodeEnum.of(code);
        if(errorCode == null) {
            throw new MessageAttributeException("ErrorCode Attribute Error. Unknown ErrorCode.");
        }

        this.errorCode = errorCode;

        // parse reason
        if(attributeData.length > 4) {
            int phraseLength = attributeData.length - 4;
            byte[] phraseBytes = new byte[phraseLength];
            System.arraycopy(attributeData,4,phraseBytes,0,phraseLength);
            String rawPhrase = new String(phraseBytes);
            this.reasonPhrase = rawPhrase.trim();
        } else {
            this.reasonPhrase = this.errorCode.getReason();
        }
        this.setValue(attributeData);
    }

    public ErrorCodeEnum getErrorCode() {
        return errorCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }
}
