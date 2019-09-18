package main.basics.crypto1.w1;


import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.math.BigInteger;
import java.util.HashMap;

public class OTPDecryptor {
    public static void main (String[] args){

        String encryptedDataBlock = "32510ba9babebbbefd001547a810e67149caee11d945cd7fc81a05e9f85aac650e9052ba6a8cd8257bf14d13e6f0a803b54fde9e77472dbff89d71b57bddef121336cb85ccb8f3315f4b52e301d16e9f52f904";
        String keyBlock = "66396E89C9DBD8CC9874352ACD6395102EAFCE78AA7FED28A07F6BC98D29C50B69B0339A19F8AA401A9C6D708F80C066C763FEF0123148CDD8E802D05BA98777335DAEFCECD59C433A6B268B60BF4EF03C9A61";
        String encryptedText = encryptedDataBlock.substring(0,keyBlock.length());

        BigInteger cTextInt = new BigInteger(encryptedText, 16);
        BigInteger keyInt = new BigInteger(keyBlock, 16);
        BigInteger resInt = cTextInt.xor(keyInt);
        String resultEncoded = resInt.toString(16);

        // Option 1: using Apache Commons Hex
        try {
            byte[] bytes = Hex.decodeHex(resultEncoded.toCharArray());
            System.out.println("Option 1 - using Appache Commons Hex: "+ new String(bytes));
        } catch (DecoderException e) {
            System.out.println("Hex decoding did not work :( " + e.getMessage());
        }

        // Option 2: using own Hex implementation
        HashMap<String,String> small = new HashMap<>();
        HashMap<String,String> capital = new HashMap<>();

        for (char cSml = 'a', cCap = 'A'; cSml <= 'z' && cCap <= 'Z'; cSml++, cCap++){
            small.put(getHex(cSml), Character.toString(cSml));
            capital.put(getHex(cCap), Character.toString(cCap));
        }

        StringBuilder textBuilder = new StringBuilder();
        for (int c = 0; c < resultEncoded.length(); c += 2)
        {
            String resultHex = resultEncoded.substring(c, c + 2);
            resultHex = resultHex.toUpperCase();
            if (small.containsKey(resultHex)) {
                textBuilder.append(small.get(resultHex));
            }
            else textBuilder.append(capital.getOrDefault(resultHex, " "));
        }
        System.out.println("Option 2 - using own Hex implementation: "+textBuilder.toString());
    }
    private static String getHex(char charToHex) {
        return String.format("%02x", (int) charToHex).toUpperCase();
    }
}
