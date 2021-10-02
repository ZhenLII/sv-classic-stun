package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;
import classicstun.message.exception.MessageAttributeException;
import common.exception.ByteUiltsException;
import common.utils.ByteUtils;

import java.lang.reflect.InvocationTargetException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author JiangZhenli
 */
public abstract class MessageAttribute {

    private final static Map<Integer,Class> ADDRESS_TYPE = Map.of(
            MessageAttributeType.MAPPED_ADDRESS.value, MappedAddress.class,
            MessageAttributeType.RESPONSE_ADDRESS.value, ResponseAddress.class,
            MessageAttributeType.SOURCE_ADDRESS.value, SourceAddress.class,
            MessageAttributeType.CHANGED_ADDRESS.value, ChangedAddress.class,
            MessageAttributeType.REFLECTED_FROM.value, ReflectedFrom.class
    );

    private MessageAttributeType type;
    int length;
    byte[] value;

    MessageAttribute(MessageAttributeType type) {
        this.type = type;
    }

    public MessageAttributeType getType() {
        return type;
    }

    public abstract byte[] encode();


    /**
     * 解析MessageHeader后的MessageAttribute
     * @param bytes 不包括MessageHeader的报文剩余数据
     */
    public static List<MessageAttribute> parse(byte[] bytes) throws MessageAttributeException, UnknownHostException, ReflectiveOperationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        if (bytes.length % 4 != 0) {
            throw new MessageAttributeException("Invalid Attributes Data");
        }
        List<MessageAttribute> attributes = new ArrayList<>();

        // 2 bytes
        int typeSize = 2;

        //2 bytes
        int lengthSize = 2;

        // 解析起始位置
        int pos = 0;
        try {

            while (pos < bytes.length) {
                // 缓存每个属性的类型
                byte[] typeData = new byte[typeSize];
                // 缓存属性数据长度
                byte[] lengthData = new byte[lengthSize];

                // 首先获取当前属性的类型
                System.arraycopy(bytes, pos, typeData, 0, typeSize);
                // 移动至长度位
                pos += typeSize;
                // 获取属性长度
                System.arraycopy(bytes, pos, lengthData, 0, lengthSize);
                //移动至数据位
                pos += lengthSize;

                // 解析类型和长度的值
                int type = ByteUtils.twoBytesToInteger(typeData);
                int length = ByteUtils.twoBytesToInteger(lengthData);

                // 根据指定长度获取属性具体数据
                byte[] value = new byte[length];
                System.arraycopy(bytes, pos, value, 0, length);


                MessageAttributeType typeEnum = MessageAttributeType.of(type);
                if(typeEnum == null) {
                    throw new MessageAttributeException("Invalid Attribute Type");
                }

                switch (typeEnum) {
                    case MAPPED_ADDRESS:
                    case RESPONSE_ADDRESS:
                    case SOURCE_ADDRESS:
                    case CHANGED_ADDRESS:
                    case REFLECTED_FROM:
                        attributes.add(decodeAddressData(type,value));
                        break;
                    // TODO Other Attributes
                    default:
                }

            }
        } catch (ByteUiltsException e) {
            e.printStackTrace();
        }
        return attributes;
    }

    private static AddressAttribute decodeAddressData(int type, byte[] addrValueData) throws MessageAttributeException, UnknownHostException, ByteUiltsException, ReflectiveOperationException {
        if(!ADDRESS_TYPE.keySet().contains(type)) {
            throw new MessageAttributeException("Address Attribute Type Error.");
        }
        if(addrValueData.length != 8) {
            throw new MessageAttributeException("Address Attribute Length Error.");
        }
        int family = addrValueData[1];
        if(family != AddressAttribute.ADDRESS_FAMILY) {
            throw new MessageAttributeException("Address Attribute Family Error.");
        }
        byte[] portData = new byte[2];
        byte[] addressData = new byte[4];
        System.arraycopy(addrValueData,3,portData,0,2);
        System.arraycopy(addrValueData,5,addressData,0,4);


        int port = ByteUtils.twoBytesToInteger(portData);
        Inet4Address ipv4Address = (Inet4Address) Inet4Address.getByAddress(addressData);
        AddressAttribute addressAttribute =
                (AddressAttribute) ADDRESS_TYPE.get(type).getConstructor(Inet4Address.class,int.class).newInstance(ipv4Address,port);

        addressAttribute.length = addrValueData.length;
        addressAttribute.value = addrValueData;

        return addressAttribute;

    }

}
