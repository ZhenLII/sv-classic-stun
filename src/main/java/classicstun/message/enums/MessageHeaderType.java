package classicstun.message.enums;

/**
 * @author JiangZhenli
 */
public enum  MessageHeaderType {

    Binding_Request(0x0001),
    Binding_Response(0x0101),
    Binding_Error_Response(0x0111),
    Shared_Secret_Request(0x0002),
    Shared_Secret_Response(0x0102),
    Shared_Secret_Error_Response(0x0112);

    int value;
    MessageHeaderType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static MessageHeaderType parse(int value) {
        for(MessageHeaderType type : values()) {
            if(type.value == value) {
                return type;
            }
        }
        return null;
    }
}
