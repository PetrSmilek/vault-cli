package com.vaultcli.dao.impl;

import com.vaultcli.dao.PasswordEntryDao;
import com.vaultcli.model.PasswordEntry;
import com.vaultcli.config.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PasswordEntryDaoImpl implements PasswordEntryDao {
    @Override
    public boolean addPasswordEntry(PasswordEntry entry) {
        String sql = "INSERT INTO password_entries (user_id, service_name, encrypted_password, iv) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, entry.getUserId());
            stmt.setString(2, entry.getServiceName());
            stmt.setString(3, entry.getEncryptedPassword());
            stmt.setString(4, entry.getIv());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Chyba při ukládání hesla: " + e.getMessage());
            return false;
        }
    }

    @Override
    public PasswordEntry getPasswordEntry(int userId, String serviceName) {
        String sql = "SELECT * FROM password_entries WHERE user_id = ? AND service_name = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, serviceName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PasswordEntry entry = new PasswordEntry();
                    entry.setId(rs.getInt("id"));
                    entry.setUserId(rs.getInt("user_id"));
                    entry.setServiceName(rs.getString("service_name"));
                    entry.setEncryptedPassword(rs.getString("encrypted_password"));
                    entry.setIv(rs.getString("iv"));
                    return entry;
                }
            }
        } catch (SQLException e) {
            System.err.println("Chyba při čtení hesla: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<PasswordEntry> getAllForUser(int userId) {
        List<PasswordEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM password_entries WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PasswordEntry entry = new PasswordEntry();
                    entry.setId(rs.getInt("id"));
                    entry.setUserId(rs.getInt("user_id"));
                    entry.setServiceName(rs.getString("service_name"));
                    entry.setEncryptedPassword(rs.getString("encrypted_password"));
                    entry.setIv(rs.getString("iv"));
                    entries.add(entry);
                }
            }
        } catch (SQLException e) {
            System.err.println("Chyba při načítání hesel: " + e.getMessage());
        }
        return entries;
    }

    @Override
    public boolean updatePasswordEntry(PasswordEntry entry) {
        String sql = "UPDATE password_entries SET encrypted_password = ?, iv = ? WHERE user_id = ? AND service_name = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entry.getEncryptedPassword());
            stmt.setString(2, entry.getIv());
            stmt.setInt(3, entry.getUserId());
            stmt.setString(4, entry.getServiceName());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Chyba při aktualizaci hesla: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deletePasswordEntry(int userId, String serviceName) {
        String sql = "DELETE FROM password_entries WHERE user_id = ? AND service_name = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, serviceName);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Chyba při mazání hesla: " + e.getMessage());
            return false;
        }
    }
}