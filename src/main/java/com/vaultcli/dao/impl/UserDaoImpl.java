package com.vaultcli.dao.impl;

import com.vaultcli.dao.UserDao;
import com.vaultcli.model.User;
import com.vaultcli.config.DatabaseManager;
import java.sql.*;

public class UserDaoImpl implements UserDao {
    @Override
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Chyba při vytváření uživatele: " + e.getMessage());
        }
        return false;
    }

    @Override
    public User getUserByUsername(String username) {
        String sql = "SELECT id, username, password_hash FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Chyba při čtení uživatele: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean userExists(String username) {
        return getUserByUsername(username) != null;
    }
}