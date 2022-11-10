package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;
import classicstun.message.exception.MessageAttributeException;
import common.exception.ByteUiltsException;
import common.utils.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author JiangZhenli
 */
public abstract class MessageAttribute {
    private static Logger log = LoggerFactory.getLogger(MessageAttribute.class);
    private final static Map<MessageAttributeType, Class<?>> MESSAGE_TYPE = new HashMap<>() {{
        MESSAGE_TYPE.put(MessageAttributeType.MAPPED_ADDRESS, MappedAddress.class);
        MESSAGE_TYPE.put(MessageAttributeType.RESPONSE_ADDRESS, ResponseAddress.class);
        MESSAGE_TYPE.put(MessageAttributeType.CHANGE_REQUEST, ChangeRequest.class);
        MESSAGE_TYPE.put(MessageAttributeType.SOURCE_ADDRESS, SourceAddress.class);
        MESSAGE_TYPE.put(MessageAttributeType.CHANGED_ADDRESS, ChangedAddress.class);
        MESSAGE_TYPE.put(MessageAttributeType.USERNAME, Username.class);
        MESSAGE_TYPE.put(MessageAttributeType.PASSWORD, Password.class);
        MESSAGE_TYPE.put(MessageAttributeType.MESSAGE_INTEGRITY, MessageIntegrity.class);
        MESSAGE_TYPE.put(MessageAttributeType.ERROR_CODE, ErrorCode.class);
        MESSAGE_TYPE.put(MessageAttributeType.UNKNOWN_ATTRIBUTES, UnknownAttribute.class);
        MESSAGE_TYPE.put(MessageAttributeType.REFLECTED_FROM, ReflectedFrom.class);
    }};

    private MessageAttributeType type;
    protected int length;
    protected byte[] value;
    protected boolean decoded = false;


    MessageAttribute(MessageAttributeType type) {
        this.type = type;
    }

    public MessageAttributeType getType() {
        return type;
    }

    abstract byte[] encode();

    /**
     * 将消息中的属性数据解析到对象中
     * @param attrValueData TLV结构中的 V value
     * */
    abstract void decode(byte[] attrValueData) throws MessageAttributeException;

    void setValue(byte[] attrValueData) {
        this.length = attrValueData.length;
        this.value = attrValueData;
        this.decoded = true;
    }

    /**
     * 解析MessageHeader后的MessageAttribute
     *
     * @param bytes 不包括MessageHeader的报文剩余数据
     */
    public static List<MessageAttribute> parse(byte[] bytes) throws MessageAttributeException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        if (bytes.length % 4 != 0) {
            throw new MessageAttributeException("Invalid Attributes Data");
        }
        List<MessageAttribute> attributes = new ArrayList<>();

        // 2 bytes
        int typeSize = 2;

        //2 bytes
        int lengthSize = 2;

        // 解析起始位置
        int pos = 0;
        try {

            while (pos < bytes.length) {
                // 缓存每个属性的类型
                byte[] typeData = new byte[typeSize];
                // 缓存属性数据长度
                byte[] lengthData = new byte[lengthSize];

                // 首先获取当前属性的类型
                System.arraycopy(bytes, pos, typeData, 0, typeSize);
                // 移动至长度位
                pos += typeSize;
                // 获取属性长度
                System.arraycopy(bytes, pos, lengthData, 0, lengthSize);
                //移动至数据位
                pos += lengthSize;

                // 解析类型和长度的值
                int type = ByteUtils.twoBytesToInteger(typeData);
                int length = ByteUtils.twoBytesToInteger(lengthData);

                // 根据指定长度获取属性具体数据
                byte[] value = new byte[length];
                System.arraycopy(bytes, pos, value, 0, length);


                MessageAttributeType typeEnum = MessageAttributeType.of(type);
                if (typeEnum == null) {
                    throw new MessageAttributeException("Invalid Attribute Type");
                }

                MessageAttribute attribute =
                        (MessageAttribute) MESSAGE_TYPE.get(typeEnum).getConstructor().newInstance();
                attribute.decode(value);
                attributes.add(attribute);

            }
        } catch (ByteUiltsException | ReflectiveOperationException e) {
            log.error(e.getMessage(),e);
            throw new MessageAttributeException("Message Attribute Parse Error");
        }
        return attributes;
    }

}
