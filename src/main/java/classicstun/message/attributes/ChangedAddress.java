package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;

import java.net.Inet4Address;

/**
 * @author JiangZhenli
 */
public class ChangedAddress extends AddressAttribute{
    public ChangedAddress(int port, Inet4Address ipAddress) {
        super(MessageAttributeType.CHANGED_ADDRESS, port, ipAddress);
    }

    @Override
    public byte[] encode() {
        return new byte[0];
    }

    public static ChangedAddress decode(byte[] bytes) {
        return null;
    }
}
