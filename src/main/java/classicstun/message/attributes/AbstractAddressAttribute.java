package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;
import classicstun.message.exception.MessageAttributeException;
import common.exception.ByteUiltsException;
import common.utils.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.List;


/**
 * @author JiangZhenli
 *
 *
 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |x x x x x x x x|    Family     |           Port                |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                             Address                           |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */
public abstract class AbstractAddressAttribute extends MessageAttribute {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private final static List<Integer> ADDRESS_TYPE = List.of(
            MessageAttributeType.MAPPED_ADDRESS.value,
            MessageAttributeType.RESPONSE_ADDRESS.value,
            MessageAttributeType.SOURCE_ADDRESS.value,
            MessageAttributeType.CHANGED_ADDRESS.value,
            MessageAttributeType.REFLECTED_FROM.value
    );

    public byte[] encodeValue() {
        byte[] bytes = new byte[8];
        bytes[1] = ADDRESS_FAMILY;
        byte[] portBytes = ByteUtils.intToByteArray(port);
        System.arraycopy(portBytes,2,bytes,2,2);
        if(ipAddress != null) {
            System.arraycopy(ipAddress.getAddress(),0,bytes,4,4);
        }
        return bytes;
    }


    public void decode(byte[] attrValueData) throws MessageAttributeException {
        decodeAddressData(attrValueData);
    }

    // ipv4
    public static byte ADDRESS_FAMILY = 0x01;

    protected int port;
    protected Inet4Address ipAddress;

    AbstractAddressAttribute(MessageAttributeType type, int port, Inet4Address ipAddress) {
        super(type);
        this.port = port;
        this.ipAddress = ipAddress;
    }




    void decodeAddressData(byte[] addrValueData) throws MessageAttributeException {

        if(addrValueData.length != 8) {
            throw new MessageAttributeException("Address Attribute Length Error.");
        }
        byte family = addrValueData[1];
        if(family != AbstractAddressAttribute.ADDRESS_FAMILY) {
            throw new MessageAttributeException("Address Attribute Family Error.");
        }
        byte[] portData = new byte[2];
        byte[] addressData = new byte[4];
        System.arraycopy(addrValueData,3,portData,0,2);
        System.arraycopy(addrValueData,5,addressData,0,4);

        try {
            this.port= ByteUtils.twoBytesToInteger(portData);
            this.ipAddress = (Inet4Address) Inet4Address.getByAddress(addressData);
            this.setValue(addressData);
        } catch (ByteUiltsException | UnknownHostException e) {
            log.error(e.getMessage(),e);
            throw new MessageAttributeException("Address Attribute Parse Error");
        }

    }

    public int getPort() {
        return port;
    }

    public Inet4Address getIpAddress() {
        return ipAddress;
    }
}
