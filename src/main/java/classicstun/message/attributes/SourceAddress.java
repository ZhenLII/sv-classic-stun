package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;

import java.net.Inet4Address;

/**
 * @author JiangZhenli
 */
public class SourceAddress extends AddressAttribute{
    public SourceAddress(int port, Inet4Address ipAddress) {
        super(MessageAttributeType.SOURCE_ADDRESS, port, ipAddress);
    }

    @Override
    public byte[] encode() {
        return new byte[0];
    }

    public static SourceAddress decode(byte[] bytes) {
        return null;
    }
}
