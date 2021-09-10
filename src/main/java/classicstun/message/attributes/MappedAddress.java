package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;

/**
 * @author JiangSenwei
 */
public class MappedAddress extends MessageAttribute {

    private static int addressFamily =  0x01; // ipv4
    private int port;
    private String ipAddress;

    public MappedAddress(int port,String ipAddress) {
        super(MessageAttributeType.MAPPED_ADDRESS);
        this.port = port;
        this.ipAddress = ipAddress;
    }

    @Override
    public byte[] encode() {
        return new byte[0];
    }

    public static MappedAddress decode(byte[] bytes) {
        return null;
    }

}
