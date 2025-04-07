package com.vaultcli.service;

import com.vaultcli.model.PasswordEntry;
import com.vaultcli.dao.PasswordEntryDao;
import com.vaultcli.dao.impl.PasswordEntryDaoImpl;
import com.vaultcli.util.EncryptionUtil;

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
            props.load(input);
            String key = props.getProperty(ENCRYPTION_KEY_PROPERTY).trim();
            System.out.println(key);

            if (key.length() != 16 && key.length() != 24 && key.length() != 32) {
                throw new IllegalStateException("Klíč musí mít přesně 16, 24 nebo 32 znaků! Aktuální délka: " + key.length());
            }
            return key;
        } catch (Exception e) {
            throw new RuntimeException("Chyba při načítání klíče: " + e.getMessage(), e);
        }
    }

    public boolean addPassword(int userId, String serviceName, String plainPassword) {
        if (passwordExists(userId, serviceName)) {
            System.err.println("Heslo pro službu " + serviceName + " již existuje!");
            return false;
        }

        try {
            String encryptedData = EncryptionUtil.encrypt(plainPassword, encryptionKey);
            String[] parts = encryptedData.split(":");

            PasswordEntry entry = new PasswordEntry();
            entry.setUserId(userId);
            entry.setServiceName(serviceName);
            entry.setEncryptedPassword(parts[1]);
            entry.setIv(parts[0]);

            return passwordEntryDao.addPasswordEntry(entry);
        } catch (Exception e) {
            System.err.println("Chyba při šifrování hesla: " + e.getMessage());
            return false;
        }
    }

    public String getPassword(int userId, String serviceName) {
        PasswordEntry entry = passwordEntryDao.getPasswordEntry(userId, serviceName);
        if (entry == null) {
            System.err.println("Heslo pro službu " + serviceName + " nebylo nalezeno");
            return null;
        }

        try {
            String encryptedData = entry.getIv() + ":" + entry.getEncryptedPassword();
            return EncryptionUtil.decrypt(encryptedData, encryptionKey);
        } catch (Exception e) {
            System.err.println("Chyba při dešifrování hesla: " + e.getMessage());
            return null;
        }
    }

    public boolean updatePassword(int userId, String serviceName, String newPlainPassword) {
        if (!passwordExists(userId, serviceName)) {
            System.err.println("Heslo pro službu " + serviceName + " neexistuje!");
            return false;
        }

        try {
            String encryptedData = EncryptionUtil.encrypt(newPlainPassword, encryptionKey);
            String[] parts = encryptedData.split(":");

            PasswordEntry entry = new PasswordEntry();
            entry.setUserId(userId);
            entry.setServiceName(serviceName);
            entry.setEncryptedPassword(parts[1]);
            entry.setIv(parts[0]);

            return passwordEntryDao.updatePasswordEntry(entry);
        } catch (Exception e) {
            System.err.println("Chyba při aktualizaci hesla: " + e.getMessage());
            return false;
        }
    }

    public boolean deletePassword(int userId, String serviceName) {
        if (!passwordExists(userId, serviceName)) {
            System.err.println("Heslo pro službu " + serviceName + " neexistuje!");
            return false;
        }
        return passwordEntryDao.deletePasswordEntry(userId, serviceName);
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