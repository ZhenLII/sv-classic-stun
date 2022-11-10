package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;
import classicstun.message.exception.MessageAttributeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;

/**
 * @author JiangZhenli
 */
public class ReflectedFrom extends AbstractAddressAttribute {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    ReflectedFrom() {
        super(MessageAttributeType.REFLECTED_FROM, 0, null);
    }

    public ReflectedFrom(int port, Inet4Address ipAddress) {
        super(MessageAttributeType.REFLECTED_FROM, port, ipAddress);
    }

}
