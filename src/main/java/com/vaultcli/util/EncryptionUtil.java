package com.vaultcli.util;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionUtil {
    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16; // Initialization Vector

    public static String encrypt(String plaintext, String base64Key) throws Exception {
        byte[] key = Base64.getDecoder().decode(base64Key);
        SecretKey secretKey = new SecretKeySpec(key, "AES");

        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        byte[] encrypted = cipher.doFinal(plaintext.getBytes());
        String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);
        String ivBase64 = Base64.getEncoder().encodeToString(iv);

        return ivBase64 + ":" + encryptedBase64; // Formát: IV:ENCRYPTED_TEXT
    }

    public static String decrypt(String encryptedData, String base64Key) throws Exception {
        String[] parts = encryptedData.split(":");
        if (parts.length != 2) throw new IllegalArgumentException("Neplatný formát šifrovaných dat!");

        byte[] key = Base64.getDecoder().decode(base64Key);
        SecretKey secretKey = new SecretKeySpec(key, "AES");

        byte[] iv = Base64.getDecoder().decode(parts[0]);
        byte[] encrypted = Base64.getDecoder().decode(parts[1]);

        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted);
    }
}