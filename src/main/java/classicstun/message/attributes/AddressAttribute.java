package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;
import classicstun.message.exception.MessageAttributeException;
import common.exception.ByteUiltsException;
import common.utils.ByteUtils;

import java.net.*;

/**
 * @author JiangZhenli
 */

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
public abstract class AddressAttribute extends MessageAttribute {

    // ipv4
    public static int ADDRESS_FAMILY = 0x01;

    protected int port;
    protected Inet4Address ipAddress;

    AddressAttribute(MessageAttributeType type, int port, Inet4Address ipAddress) {
        super(type);
        this.port = port;
        this.ipAddress = ipAddress;
    }


    @Override
    public byte[] encode() {
        return new byte[0];
    }






}
