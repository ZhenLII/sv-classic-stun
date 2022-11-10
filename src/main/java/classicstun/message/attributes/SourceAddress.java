package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;
import classicstun.message.exception.MessageAttributeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;

/**
 * @author JiangZhenli
 */
public class SourceAddress extends AbstractAddressAttribute{

    private Logger log = LoggerFactory.getLogger(this.getClass());

    SourceAddress() {
        super(MessageAttributeType.SOURCE_ADDRESS, 0, null);
    }

    public SourceAddress(int port, Inet4Address ipAddress) {
        super(MessageAttributeType.SOURCE_ADDRESS, port, ipAddress);
    }

}
