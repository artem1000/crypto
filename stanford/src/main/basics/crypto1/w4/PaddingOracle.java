package basics.crypto1.w4;

import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class PaddingOracle {

    private static final String CIPHER_TEXT = "f20bdba6ff29eed7b046d1df9fb7000058b1ffb4210a580f748b4ac714c001bd4a61044426fb515dad3f21f18aa577c0bdf302936266926ff37dbf7035d5eeb4";
    private static final String URL = "http://crypto-class.appspot.com/po?er=";
    private static final int BLOCK_LEN = 32;

    public static void main(String[] args) {
        StringBuilder decrypted = new StringBuilder();
            final AtomicInteger counter = new AtomicInteger(0);
            Object[] dataChunks = CIPHER_TEXT.chars().mapToObj(i -> String.valueOf((char)i)).collect(Collectors.groupingBy(it -> counter.getAndIncrement()/BLOCK_LEN, Collectors.joining())).values().toArray();

            StringBuilder challengeCtext = new StringBuilder();
            StringBuilder clearedBlocks = new StringBuilder();

            for (int blkIndex = 0; blkIndex < dataChunks.length - 1 ; blkIndex++) {
                String curChunk = (String)dataChunks[blkIndex];
                String nextChunk = (String)dataChunks[blkIndex + 1];
                StringBuilder resolvedChallenge = new StringBuilder();
                StringBuilder decryptedBlock = new StringBuilder();

                inspectBytes(challengeCtext, clearedBlocks, curChunk, nextChunk, resolvedChallenge, decryptedBlock);

                clearedBlocks.append(dataChunks[blkIndex]);
                decrypted.append(decryptedBlock.reverse().toString());
                System.out.println(decrypted.toString());
            }
    }

    private static void inspectBytes(StringBuilder challengeCtext, StringBuilder clearedBlocks, String curChunk, String nextChunk, StringBuilder resolvedChallenge, StringBuilder decryptedBlock) {
        for (int pad = 1; pad <= BLOCK_LEN/2; pad++){

            String workedByte = curChunk.substring(curChunk.length() - pad * 2, curChunk.length() - pad * 2 + 2);
            byte[] workedByteInt = DatatypeConverter.parseHexBinary(workedByte);
            int workedByteXORedPad = workedByteInt[0] ^ pad;

            for (int i = 0; i < 256; i++) {
                String iEven = Integer.toHexString(i);
                iEven = StringUtils.leftPad(iEven, 2, '0');

                int challengeOracleByte = workedByteXORedPad ^ i;
                String incHex = Integer.toHexString(challengeOracleByte);
                String cTextByte = getCipherByte(incHex);
                challengeCtext.delete(0, challengeCtext.length());
                challengeCtext
                        .append(clearedBlocks.toString())
                        .append(curChunk, 0, curChunk.length() - pad * 2)
                        .append(cTextByte)
                        .append(resolvedChallenge.toString())
                        .append(nextChunk);

                URL url = getUrl(challengeCtext.toString());
                int responseCode = url != null ? makeHttpCall(url) : -1;

                if (responseCode == HttpURLConnection.HTTP_NOT_FOUND || (responseCode == HttpURLConnection.HTTP_OK && pad > 8)) {
                    System.out.println(String.format("I => %s; Clear Text TextByte => %s; Used Pad => %s; Challenge OracleByte => %s; Printable ClearText Byte => %s; Resolved Challenge Byte for the next round => %s",
                            i, Integer.toHexString(i),pad, incHex, new String(DatatypeConverter.parseHexBinary(iEven)), Integer.toHexString(i ^ workedByteInt[0] ^ (pad + 1))));
                    byte[] ar = DatatypeConverter.parseHexBinary(cTextByte + resolvedChallenge.toString());
                    resolvedChallenge.delete(0,resolvedChallenge.length());
                    appendToResolvedChallenge(resolvedChallenge, pad, ar);

                    decryptedBlock.append(new String(DatatypeConverter.parseHexBinary(iEven)));
                    break;
                }
            }
        }
    }

    private static java.net.URL getUrl(String challengeCtext) {
        try {
            return new URL(URL + challengeCtext);
        } catch (MalformedURLException e) {
            System.out.println(String.format("Malformed URL exception %s", e.getMessage()));
            return null;
        }
    }

    private static void appendToResolvedChallenge(StringBuilder resolvedChallenge, int pad, byte[] ar) {

        for (byte oneByte : ar) {
            int c = oneByte ^ pad ^ (pad + 1);
            String hex = Integer.toHexString(c);
            hex = StringUtils.leftPad(hex, 2, '0');
            resolvedChallenge.append(hex.substring(hex.length() - 2));
        }
    }

    private static String getCipherByte(String incHex) {
        if (incHex.length() > 2) {
            return incHex.substring(incHex.length() - 2);
        } else {
             return StringUtils.leftPad(incHex, 2, '0');
        }
    }

    private static int makeHttpCall(java.net.URL url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            return connection.getResponseCode();
        } catch (IOException e) {
            System.out.println(String.format("Connection to %s unsuccesfull %s", url.toString(), e.getMessage()));
            return -1;
        }
    }
}


