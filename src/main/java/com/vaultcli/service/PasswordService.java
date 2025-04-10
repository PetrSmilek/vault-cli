package com.vaultcli.service;

import com.vaultcli.exceptions.DataAccessException;
import com.vaultcli.exceptions.DuplicateEntryException;
import com.vaultcli.exceptions.EncryptionException;
import com.vaultcli.exceptions.PasswordNotFoundException;
import com.vaultcli.model.PasswordEntry;
import com.vaultcli.dao.PasswordEntryDao;
import com.vaultcli.dao.impl.PasswordEntryDaoImpl;
import com.vaultcli.util.EncryptionUtil;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Properties;
import java.io.InputStream;

public class PasswordService {
    private static final String CONFIG_FILE = "/config.properties";
    private static final String ENCRYPTION_KEY_PROPERTY = "encryption.key";

    private final PasswordEntryDao passwordEntryDao;
    private final String encryptionKey;

    public PasswordService() {
        this(new PasswordEntryDaoImpl(), loadEncryptionKey());
    }

    public PasswordService(PasswordEntryDao passwordEntryDao, String encryptionKey) {
        this.passwordEntryDao = passwordEntryDao;
        this.encryptionKey = encryptionKey;
    }

    private static String loadEncryptionKey() {
        Properties props = new Properties();
        try (InputStream input = PasswordService.class.getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IllegalStateException("Konfigurační soubor " + CONFIG_FILE + " nebyl nalezen");
            }

            props.load(input);
            String key = props.getProperty(ENCRYPTION_KEY_PROPERTY).trim();
            System.out.println(key);

            if (key.length() != 16) {
                throw new IllegalStateException("Klíč musí mít přesně 16 znaků! Aktuální délka: " + key.length());
            }
            return key;
        } catch (IOException e) {
            throw new RuntimeException("Chyba při načítání klíče: " + e.getMessage(), e);
        }
    }

    public boolean addPassword(int userId, String serviceName, String plainPassword) {
        if (passwordExists(userId, serviceName)) {
            throw new DuplicateEntryException(serviceName);
        }

        try {
            String encryptedData = EncryptionUtil.encrypt(plainPassword, encryptionKey);
            String[] parts = encryptedData.split(":");

            PasswordEntry entry = new PasswordEntry();
            entry.setUserId(userId);
            entry.setServiceName(serviceName);
            entry.setEncryptedPassword(parts[1]);
            entry.setIv(parts[0]);

            boolean success = passwordEntryDao.addPasswordEntry(entry);
            if (!success) {
                throw new DataAccessException("Nepodařilo se přidat heslo pro službu '" + serviceName + "' pro uživatele ID " + userId);
            }
            return success;
        } catch (EncryptionException e) {
            throw new EncryptionException("Služba '" + serviceName + "' chyba: " + e.getMessage());
        }
    }

    public String getPassword(int userId, String serviceName) {
        PasswordEntry entry = passwordEntryDao.getPasswordEntry(userId, serviceName);
        if (entry == null) {
            throw new PasswordNotFoundException(serviceName);
        }

        try {
            String encryptedData = entry.getIv() + ":" + entry.getEncryptedPassword();
            return EncryptionUtil.decrypt(encryptedData, encryptionKey);
        } catch (EncryptionException e) {
            throw new EncryptionException("Služba '" + serviceName + "' chyba: " + e.getMessage());
        }
    }

    public boolean updatePassword(int userId, String serviceName, String newPlainPassword) {
        if (!passwordExists(userId, serviceName)) {
            throw new PasswordNotFoundException(serviceName);
        }

        try {
            String encryptedData = EncryptionUtil.encrypt(newPlainPassword, encryptionKey);
            String[] parts = encryptedData.split(":");

            PasswordEntry entry = new PasswordEntry();
            entry.setUserId(userId);
            entry.setServiceName(serviceName);
            entry.setEncryptedPassword(parts[1]);
            entry.setIv(parts[0]);

            boolean success = passwordEntryDao.updatePasswordEntry(entry);
            if (!success) {
                throw new DataAccessException("Nepodařilo se aktualizovat heslo pro službu '" + serviceName + "' pro uživatele ID " + userId);
            }
            return success;
        } catch (EncryptionException e) {
            throw new EncryptionException("Služba '" + serviceName + "' chyba: " + e.getMessage());
        }
    }

    public boolean deletePassword(int userId, String serviceName) {
        if (!passwordExists(userId, serviceName)) {
            throw new PasswordNotFoundException(serviceName);
        }

        boolean success = passwordEntryDao.deletePasswordEntry(userId, serviceName);
        if (!success) {
            throw new DataAccessException("Nepodařilo se smazat heslo pro službu '" + serviceName + "' pro uživatele ID " + userId);
        }
        return success;
    }

    public boolean passwordExists(int userId, String serviceName) {
        return passwordEntryDao.getPasswordEntry(userId, serviceName) != null;
    }

    public List<String> getAllServiceNamesForUser(int userId) {
        return passwordEntryDao.getAllForUser(userId).stream()
                .map(PasswordEntry::getServiceName)
                .collect(Collectors.toList());
    }
}