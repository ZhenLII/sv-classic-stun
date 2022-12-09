package common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

/**
 * @author JiangZhenli
 */
public class HMAC_SHA1Utils {
    private static Logger log = LoggerFactory.getLogger(HMAC_SHA1Utils.class);
    private static final String ALGORITHM = "HmacSHA1";

    public static byte[] hmac(byte[] text, String key) {
        try {
            byte[]  keyBytes = key.getBytes();
            SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(secretKey);
            return mac.doFinal(text);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            log.error("Fail to compute hmac value.");
        }
        return null;
    }

    public static boolean validate(byte[] plain, byte[] cipher, String key) {
        return Arrays.equals(hmac(plain,key),cipher);
    }
}
