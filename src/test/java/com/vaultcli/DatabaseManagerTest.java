package com.vaultcli;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManagerTest {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/vaultcli"; // Připojení k databázi
        String user = "vault_user";
        String password = "heslo123";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            if (conn != null) {
                System.out.println("✅ Úspěšné připojení k databázi PostgreSQL!");
            } else {
                System.out.println("❌ Připojení se nezdařilo.");
            }
        } catch (SQLException e) {
            System.out.println("Chyba při připojení k databázi: " + e.getMessage());
        }
    }
}
