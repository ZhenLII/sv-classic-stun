package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;

import java.net.Inet4Address;

/**
 * @author JiangZhenli
 */
public class ReflectedFrom extends AddressAttribute {
    public ReflectedFrom(int port, Inet4Address ipAddress) {
        super(MessageAttributeType.REFLECTED_FROM, port, ipAddress);
    }

    @Override
    public byte[] encode() {
        return new byte[0];
    }

    public static ReflectedFrom decode(byte[] bytes) {
        return null;
    }
}
