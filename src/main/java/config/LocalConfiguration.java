package config;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @author JiangZhenli
 */
public class LocalConfiguration {

    public static String SECTION_CLASSIC_STUN = "classic-stun";
    private static Map<String, Map<String,String>> configurations = new ConcurrentHashMap<>();

    private static final Pattern SECTION_PATTERN = Pattern.compile("^\\[\\S+]$");
    private static final Pattern KV_PATTERN = Pattern.compile("^\\S+\\s*=\\s*\\S*$");
    private static final Pattern KV_QUOTE_PATTERN = Pattern.compile("^\\S+\\s*=\\s*\".*\"$");

    static {
        try {
            load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File("server.conf")));
        String line;
        String currSection = null;
        Map<String,String> currConfiguration = null;
        while ((line = reader.readLine()) != null) {
            if(SECTION_PATTERN.matcher(line).matches()) {
                if(currSection != null) {
                    configurations.put(currSection,currConfiguration);
                    currSection = null;
                    currConfiguration = null;
                } else {
                    currSection = line.substring(1,line.length() - 1);
                }
            } else if(KV_PATTERN.matcher(line).matches() || KV_QUOTE_PATTERN.matcher(line).matches()) {
                if(currSection == null) {
                    continue;
                }
                if(currConfiguration == null) {
                    currConfiguration = new HashMap<>();
                }
                String[] pieces = line.split("=");
                String key = pieces[0].trim();
                String value ;
                if(KV_PATTERN.matcher(line).matches()) {
                    value = pieces[1];
                } else {
                    value = pieces[1].trim().substring(1,pieces[1].length()-1);
                }
                currConfiguration.put(key,value);
            }
        }
        if(currSection != null) {
            configurations.put(currSection,currConfiguration);
        }
    }

    public static String get(ConfigEnum config) {

        Map<String,String> configuration = configurations.get(config.section);
        if(configuration != null) {
            return configuration.get(config.key);
        }
        return config.defaultValue;
    }

    public enum ConfigEnum {

        STUN_USERNAME_KEY(SECTION_CLASSIC_STUN,"UsernamePrivateKey","classic-stun"),
        STUN_PASSWORD_KEY(SECTION_CLASSIC_STUN,"PasswordPrivateKey","classic-stun"),
        STUN_DEFAULT_ADDRESS(SECTION_CLASSIC_STUN,"DefaultAddress","0.0.0.0"),
        STUN_DEFAULT_PORT(SECTION_CLASSIC_STUN,"DefaultPort","13478"),
        STUN_CHANGED_ADDRESS(SECTION_CLASSIC_STUN,"ChangedAddress","127.0.0.1"),
        STUN_CHANGED_PORT(SECTION_CLASSIC_STUN,"ChangedPort","13479"),

        ;

        String section;
        String key;
        String defaultValue;


        ConfigEnum(String section,String key, String defaultValue) {
            this.section = section;
            this.key = key;
            this.defaultValue = defaultValue;
        }
    }

}
