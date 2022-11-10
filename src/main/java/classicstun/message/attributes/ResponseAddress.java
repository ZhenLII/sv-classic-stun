package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;
import classicstun.message.exception.MessageAttributeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;

/**
 * @author JiangZhenli
 */
public class ResponseAddress extends AbstractAddressAttribute {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    ResponseAddress() {
        super(MessageAttributeType.RESPONSE_ADDRESS, 0, null);
    }

    public ResponseAddress(int port, Inet4Address ipAddress) {
        super(MessageAttributeType.RESPONSE_ADDRESS, port, ipAddress);
    }

}
