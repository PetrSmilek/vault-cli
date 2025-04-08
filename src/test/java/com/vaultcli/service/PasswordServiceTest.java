package com.vaultcli.service;

import com.vaultcli.dao.PasswordEntryDao;
import com.vaultcli.model.PasswordEntry;
import com.vaultcli.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock
    private PasswordEntryDao mockDao;

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService(mockDao, "16ByteKey1234567");
    }

    @Test
    void addPasswordSuccessfully() {
        when(mockDao.getPasswordEntry(1, "service1")).thenReturn(null);

        try (MockedStatic<EncryptionUtil> encryptionUtilMocked = mockStatic(EncryptionUtil.class)) {
            encryptionUtilMocked.when(() -> EncryptionUtil.encrypt("plainPassword", "16ByteKey1234567"))
                    .thenReturn("iv:encryptedPassword");

            when(mockDao.addPasswordEntry(any(PasswordEntry.class))).thenReturn(true);

            assertTrue(passwordService.addPassword(1, "service1", "plainPassword"));

            encryptionUtilMocked.verify(() -> EncryptionUtil.encrypt("plainPassword", "16ByteKey1234567"));
        }
    }

    @Test
    void addPasswordWhenPasswordExists() {
        when(mockDao.getPasswordEntry(1, "service1")).thenReturn(new PasswordEntry());

        assertFalse(passwordService.addPassword(1, "service1", "plainPassword"));
    }

    @Test
    void getPasswordSuccessfully() {
        PasswordEntry mockEntry = new PasswordEntry();
        mockEntry.setIv("iv");
        mockEntry.setEncryptedPassword("encryptedPassword");
        when(mockDao.getPasswordEntry(1, "service1")).thenReturn(mockEntry);

        try (MockedStatic<EncryptionUtil> encryptionUtilMocked = mockStatic(EncryptionUtil.class)) {
            encryptionUtilMocked.when(() -> EncryptionUtil.decrypt("iv:encryptedPassword", "16ByteKey1234567"))
                    .thenReturn("plainPassword");

            assertEquals("plainPassword", passwordService.getPassword(1, "service1"));

            encryptionUtilMocked.verify(() -> EncryptionUtil.decrypt("iv:encryptedPassword", "16ByteKey1234567"));
        }
    }

    @Test
    void updatePasswordSuccessfully() {
        when(mockDao.getPasswordEntry(1, "service1")).thenReturn(new PasswordEntry());

        try (MockedStatic<EncryptionUtil> encryptionUtilMocked = mockStatic(EncryptionUtil.class)) {
            encryptionUtilMocked.when(() -> EncryptionUtil.encrypt("newPlainPassword", "16ByteKey1234567"))
                    .thenReturn("iv:encryptedPassword");

            when(mockDao.updatePasswordEntry(any(PasswordEntry.class))).thenReturn(true);

            assertTrue(passwordService.updatePassword(1, "service1", "newPlainPassword"));

            encryptionUtilMocked.verify(() -> EncryptionUtil.encrypt("newPlainPassword", "16ByteKey1234567"));
        }
    }

    @Test
    void deletePasswordSuccessfully() {
        when(mockDao.getPasswordEntry(1, "service1")).thenReturn(new PasswordEntry());

        when(mockDao.deletePasswordEntry(1, "service1")).thenReturn(true);

        assertTrue(passwordService.deletePassword(1, "service1"));
    }
}
