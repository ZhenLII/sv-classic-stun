package classicstun.message.enums;

/**
 * @author JiangSenwei
 */
public enum MessageAttributeType {

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

    private int value;

    MessageAttributeType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
