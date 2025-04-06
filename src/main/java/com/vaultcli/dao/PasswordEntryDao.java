package com.vaultcli.dao;

import java.util.List;
import com.vaultcli.model.PasswordEntry;

public interface PasswordEntryDao {
    boolean addPasswordEntry(PasswordEntry entry);
    PasswordEntry getPasswordEntry(int userId, String serviceName);
    boolean updatePasswordEntry(PasswordEntry entry);
    boolean deletePasswordEntry(int userId, String serviceName);
    List<PasswordEntry> getAllForUser(int userId);
}