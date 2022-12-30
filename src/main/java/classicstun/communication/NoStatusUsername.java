package classicstun.communication;

import common.utils.ByteUtils;
import common.utils.HMAC_SHA1Utils;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * @author JiangZhenli
 */
public class NoStatusUsername {

    private String prefix;
    private Long tenMinuteTimesExpireTimestamp;
    private Inet4Address clientAddress;
    private String hmacHexStr;
    private String username;


    private NoStatusUsername(String prefix, Long tenMinuteTimesExpireTimestamp, Inet4Address clientAddress, String hmacHexStr, String username) {
        this.prefix = prefix;
        this.tenMinuteTimesExpireTimestamp = tenMinuteTimesExpireTimestamp;
        this.clientAddress = clientAddress;
        this.hmacHexStr = hmacHexStr;
        this.username = username;
    }

    private static final String[] GENERATE_SOURCE = new String[]{"0", "1", "2", "3", "4", "5", "6", "7",
            "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z"};

    public static final String SEPARATOR = "-";
    public static final int MIN_PREFIX_BYTE_LENGTH = 6;
    public static final long DEFAULT_EXPIRED_TIME = 600000;
    public static final int HMAC_SHA1_LENGTH = 20;
    public static Pattern pattern = Pattern.compile("[A-Z0-9]{6,}-\\d+-[0-9A-F]{8}-[0-9A-F]{40}");


    private NoStatusUsername(Inet4Address clientAddress,Long expiredTime,String key) {
        this.clientAddress = clientAddress;
        this.tenMinuteTimesExpireTimestamp = (System.currentTimeMillis() / DEFAULT_EXPIRED_TIME ) + (expiredTime < DEFAULT_EXPIRED_TIME ? 1 : (expiredTime / DEFAULT_EXPIRED_TIME));
        int separatorLength = SEPARATOR.getBytes().length;
        String addressHex = ByteUtils.byteArrayToHexStr(clientAddress.getAddress());
        String expiredTimeStr = tenMinuteTimesExpireTimestamp.toString();

        int prefixLength = MIN_PREFIX_BYTE_LENGTH + (4 - (MIN_PREFIX_BYTE_LENGTH + separatorLength +
                expiredTimeStr.getBytes().length + separatorLength +
                addressHex.getBytes().length + separatorLength +
                HMAC_SHA1_LENGTH ) % 4);

        this.prefix = randomPrefix(prefixLength);
        String rawUsername = prefix + SEPARATOR + expiredTimeStr + SEPARATOR + addressHex + SEPARATOR;
        this.hmacHexStr = ByteUtils.byteArrayToHexStr(HMAC_SHA1Utils.hmac(rawUsername.getBytes(),key));
        this.username =  rawUsername + hmacHexStr;
    }

    public static NoStatusUsername create(Inet4Address clientAddress, Long expiredTime,String key) {
        return new NoStatusUsername(clientAddress,expiredTime,key);
    }

    public static NoStatusUsername create(Inet4Address clientAddress,String key) {
        return new NoStatusUsername(clientAddress,DEFAULT_EXPIRED_TIME,key);
    }

    public static NoStatusUsername parse(String username, String key) {
        if(!isNoStatusUsername(username)) {
           throw new IllegalArgumentException("Wrong username: " + username);
        }

        String[] pieces = username.split(SEPARATOR);
        String prefix = pieces[0];
        String timeStr = pieces[1];
        String addressHex = pieces[2];
        String hmacHex = pieces[3];

        try {

            String plain = username.substring(0, username.length() - hmacHex.length());
            if(!HMAC_SHA1Utils.validate(plain.getBytes(),ByteUtils.hexStringToByteArray(hmacHex,null),key)) {
                throw new IllegalStateException("Username integrity error.");
            }
            Long tenMinuteTimesExpireTimestamp = Long.parseLong(timeStr);
            Inet4Address address = (Inet4Address) Inet4Address.getByAddress(ByteUtils.hexStringToByteArray(addressHex,null));
            return new NoStatusUsername(prefix,tenMinuteTimesExpireTimestamp,address,hmacHex,username);
        } catch (Exception e) {
            throw new IllegalStateException("Can not parse username: "+ username);
        }
    }

    private static String randomPrefix(Integer length) {
        List<String> list = Arrays.asList(GENERATE_SOURCE);
        Collections.shuffle(list);
        StringBuilder randomStr = new StringBuilder();
        Random random = new Random();
        while (randomStr.toString().getBytes().length < length) {
            randomStr.append(list.get(random.nextInt(GENERATE_SOURCE.length)));
        }
        return randomStr.toString();
    }

    public static boolean isNoStatusUsername(String username) {
        return pattern.matcher(username).matches();
    }

    public boolean expired() {
        return System.currentTimeMillis() / DEFAULT_EXPIRED_TIME > tenMinuteTimesExpireTimestamp;
    }

    public String getPrefix() {
        return prefix;
    }

    public String calculatePassword(String privateKey) {
        return ByteUtils.byteArrayToHexStr(HMAC_SHA1Utils.hmac(this.username.getBytes(),privateKey));
    }

    public Long getTenMinuteTimesExpireTimestamp() {
        return tenMinuteTimesExpireTimestamp;
    }

    public Inet4Address getClientAddress() {
        return clientAddress;
    }

    public String getHmacHexStr() {
        return hmacHexStr;
    }

    public String getUsername() {
        return username;
    }

}
