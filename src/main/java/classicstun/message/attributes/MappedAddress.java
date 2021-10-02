package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;

import java.net.Inet4Address;

/**
 * @author JiangZhenli
 */
public class MappedAddress extends AddressAttribute {

    public MappedAddress(int port, Inet4Address ipAddress) {
        super(MessageAttributeType.MAPPED_ADDRESS, port, ipAddress);
    }


    @Override
    public byte[] encode() {
        return new byte[0];
    }


    public static MappedAddress decode(byte[] bytes) {
        return null;
    }
}
