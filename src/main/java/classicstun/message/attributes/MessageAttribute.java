package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;
import classicstun.message.exception.MessageAttributeException;
import common.exception.ByteUiltsException;
import common.utils.ByteUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JiangSenwei
 */
public abstract class MessageAttribute {
    private MessageAttributeType type;
    int length;
    byte[] value;

    MessageAttribute(MessageAttributeType type) {
        this.type = type;
    }

    public MessageAttributeType getType() {
        return type;
    }

    public abstract byte[] encode();


    public static List<MessageAttribute> parse(byte[] bytes) throws MessageAttributeException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        if (bytes.length % 4 != 0) {
            throw new MessageAttributeException("Invalid Atrributes Data");
        }
        List<MessageAttribute> attributes = new ArrayList<>();
        int typeSize = 2; // 2 bytes
        int lengthSize = 2; //2 bytes

        int pos = 0; // 解析起始位置
        try {
            while (pos < bytes.length) {
                byte[] typeData = new byte[typeSize];
                byte[] lengthData = new byte[lengthSize];
                System.arraycopy(bytes, pos, typeData, 0, typeSize);
                pos += typeSize;
                System.arraycopy(bytes, pos, lengthData, 0, lengthSize);
                pos += lengthSize;
                int type = ByteUtils.twoBytesToInteger(typeData);
                int length = ByteUtils.twoBytesToInteger(lengthData);
                byte[] value = new byte[length];
                System.arraycopy(bytes, pos, value, 0, length);

            }
        } catch (ByteUiltsException e) {
            e.printStackTrace();
        }
        return attributes;
    }

}
