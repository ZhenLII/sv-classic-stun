package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;
import classicstun.message.exception.MessageAttributeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;

/**
 * @author JiangZhenli
 */
public class MappedAddress extends AbstractAddressAttribute {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    MappedAddress() {
        super(MessageAttributeType.MAPPED_ADDRESS, 0, null);
    }

    public MappedAddress(int port, Inet4Address ipAddress) {
        super(MessageAttributeType.MAPPED_ADDRESS, port, ipAddress);
    }


}
