package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;
import classicstun.message.exception.MessageAttributeException;
import common.exception.ByteUiltsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * @author JiangZhenli
 */
public class ChangedAddress extends AbstractAddressAttribute{

    private Logger log = LoggerFactory.getLogger(this.getClass());

    ChangedAddress() {
        super(MessageAttributeType.CHANGED_ADDRESS, 0, null);
    }

    public ChangedAddress(int port, Inet4Address ipAddress) {
        super(MessageAttributeType.CHANGED_ADDRESS, port, ipAddress);
    }

}
