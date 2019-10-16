package basics.crypto1.w3;

import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class SHAValidator {
    private static final String SHA_ALG ="SHA-256";
    private static final String TEST_FILE_URL ="https://crypto.stanford.edu/~dabo/onlineCrypto/6.2.birthday.mp4_download";
    private static final String TASK_FILE_URL ="https://crypto.stanford.edu/~dabo/onlineCrypto/6.1.intro.mp4_download";
    private static final String CONFIG = "w3.config.groovy";
    private static final String TEST_SHA256 = "3c08f4ee0b576fe319338139c045c89c3e8e9409633bea29442e21425006ea8";
    private static boolean useTestFile = true ;
    private static URL targetFile = null ;

    static {

        ConfigSlurper cs = new ConfigSlurper();
        File configurationFile = new File(Objects.requireNonNull(SHAValidator.class.getClassLoader().getResource(CONFIG)).getFile());

        if (!configurationFile.exists()) {
            System.out.println(String.format("Configuration file %s not found, exiting...", CONFIG));
            System.exit(0);
        }

        try {
            ConfigObject configObject = cs.parse(FileUtils.readFileToString(configurationFile));
            ConfigObject co = (ConfigObject) configObject.get("config");
            useTestFile = (boolean) co.getProperty("useTestFile");
            targetFile = useTestFile ? new URL(TEST_FILE_URL) : new URL(TASK_FILE_URL);
        } catch (IOException e) {
            System.out.println(String.format("IOException when loading config file: %s", e.getMessage()));
        }
    }

    public static void main(String[] args){

        List<byte[]> byteFrags = new ArrayList<>();

        try(BufferedInputStream fin = new BufferedInputStream(targetFile.openStream())) {

            MessageDigest sha = MessageDigest.getInstance(SHA_ALG);
            byte[] bucket = new byte[1024];
            int bytesRead;

            while ((bytesRead = fin.read(bucket)) != -1) {
                if(bytesRead == 1024) {
                    byte[] chunk = Arrays.copyOf(bucket, bucket.length);
                    byteFrags.add(chunk);
                }
                else {
                    byte[] temp = Arrays.copyOf(bucket,bytesRead);
                    byteFrags.add(temp);
                }
            }

            Collections.reverse(byteFrags);
            byte[] digest = null;
            byte[] result;

            for (int i = 0; i < byteFrags.size(); i++) {
                byte[] temp = Arrays.copyOf(byteFrags.get(i),byteFrags.get(i).length);

                if (digest != null ) {
                    result = concat(temp,digest);
                    sha.update(result);
                    digest = sha.digest();
                    sha.reset();
                }
                else {
                    sha.update(temp);
                    digest = sha.digest();
                    sha.reset();
                }
                if (i == byteFrags.size() - 1) {
                    String resultHex = new BigInteger(1, digest).toString(16);
                    System.out.println(StringUtils.leftPad("SHA-256 ==> "+resultHex,64,'0'));
                    if (useTestFile && !TEST_SHA256.equals(resultHex)) {
                        System.out.println("Test hash does not match");
                    }
                }
            }

        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println(String.format("IOException or NoSuchAlgorithmException caught when computing hash: %s", e.getMessage()));
        }
    }

    private static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
