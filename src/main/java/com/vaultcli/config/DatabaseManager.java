package com.vaultcli.config;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static DatabaseManager instance;
    private final String url;
    private final String user;
    private final String password;

    private DatabaseManager() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                throw new RuntimeException("Soubor application.properties nebyl nalezen v classpath");
            }

            Properties props = new Properties();
            props.load(input);

            this.url = props.getProperty("db.url");
            this.user = props.getProperty("db.user");
            this.password = props.getProperty("db.password");

            if (url == null || user == null || password == null) {
                throw new RuntimeException("Chybějící konfigurace DB v application.properties");
            }

        } catch (Exception e) {
            throw new RuntimeException("Chyba při načítání konfigurace databáze", e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}