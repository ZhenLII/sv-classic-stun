package classicstun.message;

import classicstun.message.enums.MessageHeaderType;
import classicstun.message.exception.MessageHeaderExcepion;
import common.utils.ByteUtils;

import java.util.UUID;

/**
 * 0                   1                   2                     3
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |      STUN Message Type        |         Message Length        |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *                          Transaction ID
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *                                                                 |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *
 * Message Header格式
 * 参见 RFC3489
 * @author JiangZhenli
 */
public class MessageHeader {
    private MessageHeaderType messageType;
    private int messageLength;
    private byte[] transactionId;

    private MessageHeader() {
    }

    private MessageHeader(MessageHeaderType messageType, int messageLength, byte[] transactionId) {
        if(transactionId.length != 16) {
            throw new IllegalArgumentException("The length of transactionId must be 16 (byte).");
        }
        this.messageType = messageType;
        this.messageLength = messageLength;
        this.transactionId = transactionId;
    }

    void setMessageLength(int messageLength) throws MessageHeaderExcepion {
        if (messageLength > 65535 || messageLength < 0) {
            throw new MessageHeaderExcepion("Set Length Faild: The length must < 65536 and > 0.");
        }
        this.messageLength = messageLength;
    }

    void setMessageType(MessageHeaderType messageHeaderType) {
        this.messageType = messageHeaderType;
    }



    byte[] encode() throws MessageHeaderExcepion {
        byte[] messageHeaderCode = new byte[20];
        byte[] messageTypeCode = ByteUtils.intToByteArray(messageType.value());
        messageHeaderCode[0] = messageTypeCode[2];
        messageHeaderCode[1] = messageTypeCode[3];

        byte[] messageLengthCode = ByteUtils.intToByteArray(messageLength);
        messageHeaderCode[2] = messageLengthCode[2];
        messageHeaderCode[3] = messageLengthCode[3];
        System.arraycopy(transactionId, 0, messageHeaderCode, 4, transactionId.length);
        return messageHeaderCode;
    }

    static MessageHeader decode(byte[] bytes) throws MessageHeaderExcepion {
        if (bytes.length < 20) {
            throw new MessageHeaderExcepion("Decode MessageHeader Failed");
        }
        byte[] messageTypeBytes = new byte[2];
        byte[] messageLengthBytes = new byte[2];
        byte[] messageTransactionIdBytes = new byte[16];
        int messageType;
        int messageLength;
        try {
            System.arraycopy(bytes, 0, messageTypeBytes, 0, 2);
            System.arraycopy(bytes, 2, messageLengthBytes, 0, 2);
            System.arraycopy(bytes, 4, messageTransactionIdBytes, 0, 16);
            messageType = ByteUtils.twoBytesToInteger(messageTypeBytes);
            messageLength = ByteUtils.twoBytesToInteger(messageLengthBytes);

        } catch (Exception e) {
            throw new MessageHeaderExcepion("Decode MessageHeader Failed");
        }
        MessageHeaderType type = MessageHeaderType.parse(messageType);
        if (type == null) {
            throw new MessageHeaderExcepion("Unkown Message Type");
        }
        if (messageLength >  65535 || messageLength < 0) {
            throw new MessageHeaderExcepion("Invalid Message Length");
        }

        return new MessageHeader(type, messageLength, messageTransactionIdBytes);
    }

    public MessageHeaderType getMessageType() {
        return messageType;
    }

    public int getMessageLength() {
        return messageLength;
    }

    public byte[] getTransactionId() {
        return transactionId;
    }

    // 初始化一个用于发送的MessageHeader
    public static MessageHeader init(MessageHeaderType messageHeaderType, byte[] transactionId, boolean genTransactionId) {
        MessageHeader header = new MessageHeader();
        header.messageType = messageHeaderType;
        if(transactionId != null ) {
            if( transactionId.length != 16) {
                throw new IllegalArgumentException("The length of transactionId must be 16 (byte).");
            }
            header.transactionId = transactionId;
        } else if(genTransactionId) {
            header.transactionId = ByteUtils.uuidToByteArray(UUID.randomUUID());
        }

        return header;
    }



}
