package com.vaultcli.util;

import com.vaultcli.exceptions.EncryptionException;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionUtil {
    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int KEY_LENGTH_BYTES = 16; // Pouze pro AES-128
    private static final int IV_LENGTH = 16;

    public static String encrypt(String plaintext, String rawKey) throws EncryptionException {
        try {
            validateKey(rawKey);

            byte[] keyBytes = rawKey.getBytes(StandardCharsets.UTF_8);
            SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(iv) + ":" +
                    Base64.getEncoder().encodeToString(encrypted);
        } catch (GeneralSecurityException e) {
            throw new EncryptionException(e.getMessage());
        }
    }

    public static String decrypt(String encryptedData, String rawKey) throws EncryptionException {
        try {
            validateKey(rawKey);
            byte[] keyBytes = rawKey.getBytes(StandardCharsets.UTF_8);

            String[] parts = encryptedData.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Neplatný formát šifrovaných dat!");
            }

            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encrypted = Base64.getDecoder().decode(parts[1]);

            SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new EncryptionException(e.getMessage());
        }
    }

    private static void validateKey(String key) {
        if (key == null || key.length() != KEY_LENGTH_BYTES) {
            throw new IllegalArgumentException(
                    "Klíč musí být přesně " + KEY_LENGTH_BYTES + " znaků dlouhý! Aktuální délka: " +
                            (key == null ? "null" : key.length())
            );
        }
    }
}