package com.vaultcli.util;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionUtil {
    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16; // Initialization Vector

    public static String encrypt(String plaintext, String key) throws Exception {
        String base64Key;
        if (key.length() == 16 || key.length() == 24 || key.length() == 32) {
            base64Key = Base64.getEncoder().encodeToString(key.getBytes());
        } else {
            base64Key = key;
        }

        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        validateKeyLength(keyBytes.length);

        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        byte[] encrypted = cipher.doFinal(plaintext.getBytes());
        return Base64.getEncoder().encodeToString(iv) + ":" +
                Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String encryptedData, String key) throws Exception {
        String base64Key = (key.length() == 16 || key.length() == 24 || key.length() == 32)
                ? Base64.getEncoder().encodeToString(key.getBytes())
                : key;

        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        validateKeyLength(keyBytes.length);

        String[] parts = encryptedData.split(":");
        if (parts.length != 2) throw new IllegalArgumentException("Neplatný formát šifrovaných dat!");

        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        byte[] iv = Base64.getDecoder().decode(parts[0]);
        byte[] encrypted = Base64.getDecoder().decode(parts[1]);

        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

        return new String(cipher.doFinal(encrypted));
    }

    private static void validateKeyLength(int lengthInBytes) throws Exception {
        if (lengthInBytes != 16 && lengthInBytes != 24 && lengthInBytes != 32) {
            throw new IllegalArgumentException(
                    "Neplatná délka klíče! Musí být 16, 24 nebo 32 bytů. Aktuální: " + lengthInBytes
            );
        }
    }
}