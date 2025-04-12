package com.vaultcli.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDatabaseManager {
    private static TestDatabaseManager instance;
    private final String url;
    private final String user;
    private final String password;

    public TestDatabaseManager(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public static void initialize(String url, String user, String password) {
        instance = new TestDatabaseManager(url, user, password);
    }

    public static Connection getTestConnection() throws SQLException {
        if (instance == null) {
            throw new IllegalStateException("TestDatabaseManager není inicializován");
        }
        return DriverManager.getConnection(instance.url, instance.user, instance.password);
    }
}