package classicstun.message.attributes;

import classicstun.message.enums.MessageAttributeType;
import classicstun.message.exception.MessageAttributeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author JiangZhenli
 * <p>
 * 0                   1                   2                   3
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 A B 0|
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * <p>
 * A: This is the "change IP" flag.  If true, it requests the server
 * to send the Binding Response with a different IP address than the
 * one the Binding Request was received on.
 * <p>
 * B: This is the "change port" flag.  If true, it requests the
 * server to send the Binding Response with a different port than the
 * one the Binding Request was received on.
 */
public class ChangeRequest extends MessageAttribute {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private final static byte FF = 0; // 0000
    private final static byte FT = 2; // 0010
    private final static byte TF = 4; // 0100
    private final static byte TT = 6; // 0110
    private final static List<Byte> TruthValues = List.of(FF, FT, TF, TT);

    private boolean changeIp = false;
    private boolean changePort = false;

    ChangeRequest() {
        super(MessageAttributeType.CHANGE_REQUEST);
    }

    public ChangeRequest(boolean changeIp, boolean changePort) {
        super(MessageAttributeType.CHANGE_REQUEST);
        this.changeIp = changeIp;
        this.changePort = changePort;
    }

    @Override
    public byte[] encode() {
        byte[] bytes = new byte[4];
        if(changeIp && changePort) {
            bytes[3] = TT;
        } else if(changeIp) {
            bytes[3] = TF;
        } else if(changePort) {
            bytes[3] = FT;
        } else {
            bytes[3] = FF;
        }
        return bytes;
    }

    @Override
    public void decode(byte[] attributeData) throws MessageAttributeException {
        if (attributeData.length != 4) {
            throw new MessageAttributeException("ChangeRequest Attribute Length Error.");
        }
        if (attributeData[0] != 0 ||
                attributeData[1] != 0 ||
                attributeData[2] != 0 ||
                !TruthValues.contains(attributeData[3])
        ) {
            throw new MessageAttributeException("Unrecognized ChangeRequest Attribute.");
        }

        switch (attributeData[3]) {
            case FT:
                changePort = true;
                break;
            case TF:
                changeIp = true;
                break;
            case TT:
                changePort = true;
                changeIp = true;
            default:break;
        }

        this.setValue(attributeData);
    }

    public boolean isChangeIp() {
        return changeIp;
    }

    public boolean isChangePort() {
        return changePort;
    }
}
