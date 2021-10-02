package classicstun.message.enums;

/**
 * @author JiangZhenli
 */
public enum MessageAttributeType {

    /**
     *  用于标识Message中的属性类型
     * */

    MAPPED_ADDRESS(0x0001),
    RESPONSE_ADDRESS(0x0002),
    CHANGE_REQUEST(0x0003),
    SOURCE_ADDRESS(0x0004),
    CHANGED_ADDRESS(0x0005),
    USERNAME(0x0006),
    PASSWORD(0x0007),
    MESSAGE_INTEGRITY(0x0008),
    ERROR_CODE(0x0009),
    UNKNOWN_ATTRIBUTES(0x000a),
    REFLECTED_FROM(0x000b);

    public final int value;

    MessageAttributeType(int value) {
        this.value = value;
    }

    public static MessageAttributeType of(int value) {
        for(MessageAttributeType t : values()) {
            if(t.value == value) {
                return t;
            }
        }
        return null;
    }
}
