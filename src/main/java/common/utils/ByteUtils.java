package common.utils;

import common.exception.ByteUiltsException;

import java.util.StringTokenizer;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author JiangZhenli
 */
public class ByteUtils {

    // 从高位到低位将32位整数编码成byte数组
    public static byte[] intToByteArray(int value) {
        byte[] result = new byte[4];
        result[0] = (byte) ((value >>> 24) & 0xFF);
        result[1] = (byte) ((value >>> 16) & 0xFF);
        result[2] = (byte) ((value >>> 8) & 0xFF);
        result[3] = (byte) (value & 0xFF);
        return result;
    }

    // 从高位到低位将64位整数编码成byte数组
    public static byte[] longToByteArray(long value) {
        byte[] result = new byte[8];
        result[0] = (byte) ((value >>> 56) & 0xFF);
        result[1] = (byte) ((value >>> 48) & 0xFF);
        result[2] = (byte) ((value >>> 40) & 0xFF);
        result[3] = (byte) ((value >>> 32) & 0xFF);
        result[4] = (byte) ((value >>> 24) & 0xFF);
        result[5] = (byte) ((value >>> 16) & 0xFF);
        result[6] = (byte) ((value >>> 8) & 0xFF);
        result[7] = (byte) (value & 0xFF);
        return result;
    }


    public static int twoBytesToInteger(byte[] value) throws ByteUiltsException {
        if (value.length < 2) {
            throw new ByteUiltsException("Byte array too short!");
        }
        int temp0 = value[0] & 0xFF;
        int temp1 = value[1] & 0xFF;
        return ((temp0 << 8) + temp1);
    }

    public static long fourBytesToLong(byte[] value) throws ByteUiltsException {
        if (value.length < 4) {
            throw new ByteUiltsException("Byte array too short!");
        }
        int temp0 = value[0] & 0xFF;
        int temp1 = value[1] & 0xFF;
        int temp2 = value[2] & 0xFF;
        int temp3 = value[3] & 0xFF;
        return (((long) temp0 << 24) + (temp1 << 16) + (temp2 << 8) + temp3);
    }


    // 从高位到低位将128位UUID编码成byte数组
    public static byte[] uuidToByteArray(UUID uuid) {
        byte[] result = new byte[16];
        System.arraycopy(longToByteArray(uuid.getMostSignificantBits()), 0, result, 0, 8);
        System.arraycopy(longToByteArray(uuid.getLeastSignificantBits()), 0, result, 8, 8);
        return result;
    }

    // 将16进制形式的UUID字符串转化为byte数组
    public static byte[] uuidToByteArray(String uuidStr) throws ByteUiltsException {
        if (uuidStr.indexOf('-') != -1) {
            uuidStr = uuidStr.replaceAll("-", "");
        }
        if (uuidStr.length() != 32 && isHexString(uuidStr, null)) {
            throw new ByteUiltsException("Illegal UUID String");
        }

        return hexStringToByteArray(uuidStr, null);
    }

    // 从高位到低位将byte数组编码成UUID字符串
    public static String byteArrayToUUIDString(byte[] bytes) throws ByteUiltsException {
        if (bytes.length != 32) {
            throw new ByteUiltsException("Illegal UUID byte array");
        }
        StringBuilder builder = new StringBuilder("");
        for(byte b : bytes) {
            builder.append(Integer.toHexString(b));
        }
        return builder.toString();
    }

    /**
     * 将16进制字符串转化为对应的byte数组
     * @param hexString 字符串
     * @param delimiter 16进制字符串的分隔符，如果为null，则表示没有分隔符
     *                  分隔符不能为16进制数
     * */
    public static byte[] hexStringToByteArray(String hexString, Character delimiter) throws ByteUiltsException {
        if (!isHexString(hexString, delimiter)) {
            throw new ByteUiltsException("Illegal HexString");
        }
        if (delimiter == null) {
            byte[] result = new byte[hexString.length() / 2];
            int i = 0;
            while (i < hexString.length()) {
                String byteString = hexString.substring(i, i += 2);
                result[i / 2 - 1] = (byte) Integer.parseInt(byteString, 16);
            }
            return result;
        } else {
            String delim = "" + delimiter;
            StringTokenizer st = new StringTokenizer(hexString, delim);
            byte[] result = new byte[st.countTokens()];
            for (int n = 0; st.hasMoreTokens(); n++) {
                String s = st.nextToken();
                result[n] = (byte) Integer.parseInt(s, 16);
            }
            return result;
        }

    }

    public static String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * 判断是否是16进制字符串
     * @param string 进行判断的字符串
     * @param delimiter 16进制字符串的分隔符，如果为null，则表示没有分隔符
     *                  分隔符不能为16进制数
     * */
    public static boolean isHexString(String string, Character delimiter) {
        string = string.toUpperCase();
        String delim = "";
        if (delimiter != null) {
            if ((delimiter >= '0' && delimiter <= '9') || (delimiter >= 'a' && delimiter <= 'f')
                    || (delimiter >= 'A' && delimiter <= 'F')) {
                return false;
            }
            delim += delimiter;
        }
        Pattern pattern = Pattern.compile(String.format("^([A-F|0-9]{2}%s)*[A-F|0-9]{2}$", delim));
        Matcher matcher = pattern.matcher(string);
        return matcher.matches();
    }
}
