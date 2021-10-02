package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;

import java.net.Inet4Address;

/**
 * @author JiangZhenli
 */
public class ResponseAddress extends AddressAttribute {
    public ResponseAddress(int port, Inet4Address ipAddress) {
        super(MessageAttributeType.RESPONSE_ADDRESS, port, ipAddress);
    }

    @Override
    public byte[] encode() {
        return new byte[0];
    }

    public static ResponseAddress decode(byte[] bytes) {
        return null;
    }
}
