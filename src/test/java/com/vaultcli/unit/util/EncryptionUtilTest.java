package com.vaultcli.unit.util;

import com.vaultcli.util.EncryptionUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilTest {
    private static final String VALID_KEY = "16ByteKey1234567";

    @Test
    void encrypt_decrypt_variousInputs() throws Exception {
        String[] testStrings = {
                "text",
                "dlouhý text pro testování šifrování",
                "1234567890!@#$%^&*()_+",
                "text s různými znaky, jako áčéřšč"
        };

        for (String original : testStrings) {
            String encrypted = EncryptionUtil.encrypt(original, VALID_KEY);
            String decrypted = EncryptionUtil.decrypt(encrypted, VALID_KEY);
            assertEquals(original, decrypted);
        }
    }

    @Test
    void invalidKeyLength_throwsIllegalArgumentException() {
        String shortKey = "maly_klic";
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptionUtil.encrypt("test", shortKey);
        });
    }

    @Test
    void decrypt_withDifferentKey_throwsException() throws Exception {
        String original = "text";
        String encrypted = EncryptionUtil.encrypt(original, VALID_KEY);

        String differentKey = "16ByteKey1000009";
        assertThrows(Exception.class, () -> {
            EncryptionUtil.decrypt(encrypted, differentKey);
        });
    }

    @Test
    void tamperedData_throwsException() throws Exception {
        String encrypted = EncryptionUtil.encrypt("zprava", VALID_KEY);
        String tampered = encrypted.replace('z', 's');
        assertThrows(Exception.class, () -> {
            EncryptionUtil.decrypt(tampered, VALID_KEY);
        });
    }

    @Test
    void encrypt_decrypt_emptyString_returnsEmptyString() throws Exception {
        String original = "";
        String encrypted = EncryptionUtil.encrypt(original, VALID_KEY);
        String decrypted = EncryptionUtil.decrypt(encrypted, VALID_KEY);
        assertEquals(original, decrypted);
    }

    @Test
    void encrypt_nullInput_throwsException() {
        assertThrows(NullPointerException.class, () -> {
            EncryptionUtil.encrypt(null, VALID_KEY);
        });
    }

    @Test
    void decrypt_nullInput_throwsException() {
        assertThrows(NullPointerException.class, () -> {
            EncryptionUtil.decrypt(null, VALID_KEY);
        });
    }
}