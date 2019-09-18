package main.basics.crypto1.w2;

import org.apache.commons.lang3.ArrayUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AESCBCDecrypt {

    private static final String AES_CBC_PKCS5 = "AES/CBC/PKCS5PADDING";
    private static final String AES_CTR_PKCS5 = "AES/CTR/PKCS5PADDING";

    public static void main(String[] args){
        //Inputs
        String cbcKey = "140b41b22a29beb4061bda66b6747e14";
        String ctrKey = "36f18357be4dbd77f050515c73fcf9f2";

        String[] cipherBlocksCBC = new String[]{
                "4ca00ff4c898d61e1edbf1800618fb2828a226d160dad07883d04e008a7897ee2e4b7465d5290d0c0e6c6822236e1daafb94ffe0c5da05d9476be028ad7c1d81",
                "5b68629feb8606f9a6667670b75b38a5b4832d0f26e1ab7da33249de7d4afc48e713ac646ace36e872ad5fb8a512428a6e21364b0c374df45503473c5242a253"
        };

        String[] cipherBlocksCTR = new String[]{
                "69dda8455c7dd4254bf353b773304eec0ec7702330098ce7f7520d1cbbb20fc388d1b0adb5054dbd7370849dbf0b88d393f252e764f1f5f7ad97ef79d59ce29f5f51eeca32eabedd9afa9329",
                "770b80259ec33beb2561358a9f2dc617e46218c0a53cbeca695ae45faa8952aa0e311bde9d4e01726d3184c34451"
        };

        decrypt(cbcKey, cipherBlocksCBC, AES_CBC_PKCS5);
        decrypt(ctrKey, cipherBlocksCTR, AES_CTR_PKCS5);
    }

    private static void decrypt(String key, String[] cipherBlocks, String spec) {

        try {
            byte[] keyBytes = DatatypeConverter.parseHexBinary(key);
            SecretKeySpec secret = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance(spec);
            System.out.println(spec);
            for(String cb: cipherBlocks){
                String ivText = cb.substring(0, key.length());
                String cipherText = cb.substring(key.length());
                byte[] ivByte = DatatypeConverter.parseHexBinary(ivText);
                byte[] ctByte = DatatypeConverter.parseHexBinary(cipherText);
                IvParameterSpec ivSSpec = new IvParameterSpec(ivByte);

                cipher.init(Cipher.DECRYPT_MODE, secret, ivSSpec);
                byte[] decrypted = cipher.doFinal(ctByte);
                String result = new String(DatatypeConverter.parseHexBinary(DatatypeConverter.printHexBinary(decrypted)));
                System.out.print(String.format("Line %s => %s %n", ArrayUtils.indexOf(cipherBlocks, cb) + 1, result));
            }

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            System.out.println(String.format("Decryption did not work. Caused by %s", e.getMessage()));
        }

    }
}