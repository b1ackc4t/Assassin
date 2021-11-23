package org.b1ackc4t.sender;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Crypt {

    public static final Base64.Encoder b64Encoder = Base64.getEncoder();
    public static final Base64.Decoder b64Decoder = Base64.getDecoder();

    public static byte[] encrypt(byte[] data, String key, String type) throws Exception {
        byte[] result = null;
        if (type.equals("java")) {
            byte[] raw = key.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(1, skeySpec);
            result = cipher.doFinal(data);
        }
        return result;
    }

    public static byte[] decrypt(byte[] data, String key, String type) throws Exception {
        byte[] result = null;
        if (type.equals("java")) {
            byte[] raw = key.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(2, skeySpec);
            result = cipher.doFinal(data);
        }
        return result;
    }

}
