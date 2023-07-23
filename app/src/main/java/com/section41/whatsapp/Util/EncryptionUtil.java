package com.section41.whatsapp.Util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import android.util.Base64;


public class EncryptionUtil {
    private static final String AES_MODE = "AES/CBC/PKCS7Padding";
    private static final String CHARSET_NAME = "UTF-8";

    public static String encrypt(String message, String secretKey) throws Exception {
        SecretKeySpec keySpec = generateKey(secretKey);
        Cipher cipher = Cipher.getInstance(AES_MODE);
        byte[] iv = new byte[cipher.getBlockSize()];
        Arrays.fill(iv, (byte) 0);
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParams);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        byte[] encodedBytes = Base64.encode(encryptedBytes, Base64.DEFAULT);
        return new String(encodedBytes, StandardCharsets.UTF_8);
    }

    public static String decrypt(String encryptedMessage, String secretKey) throws Exception {
        SecretKeySpec keySpec = generateKey(secretKey);
        Cipher cipher = Cipher.getInstance(AES_MODE);
        byte[] iv = new byte[cipher.getBlockSize()];
        Arrays.fill(iv, (byte) 0);
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParams);
        byte[] decodedBytes = Base64.decode(encryptedMessage, Base64.DEFAULT);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private static SecretKeySpec generateKey(String secretKey) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = secretKey.getBytes(StandardCharsets.UTF_8);
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        return new SecretKeySpec(key, "AES");
    }
}
