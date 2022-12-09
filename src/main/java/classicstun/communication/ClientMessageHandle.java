package classicstun.communication;

import common.utils.ByteUtils;

/**
 * @author JiangZhenli
 */
public class ClientMessageHandle {
    private byte[] transactionId;
    private String hexTransactionId;

    public ClientMessageHandle(byte[] transactionId) {
        this.transactionId = transactionId;
        this.hexTransactionId = ByteUtils.byteArrayToHexStr(transactionId);
    }

    public byte[] getTransactionId() {
        return transactionId;
    }

    public String getHexTransactionId() {
        return hexTransactionId;
    }

    @Override
    public int hashCode() {
        return hexTransactionId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ClientMessageHandle) && (hexTransactionId.equals(((ClientMessageHandle) obj).getHexTransactionId()));
    }
}
