package classicstun.message;

import classicstun.message.attributes.MessageAttribute;
import classicstun.message.enums.MessageAttributeType;

import java.util.Map;

/**
 * @author JiangZhenli
 */
public class Message {
    MessageHeader header;
    Map<MessageAttributeType, MessageAttribute> messageAttributes;
}
