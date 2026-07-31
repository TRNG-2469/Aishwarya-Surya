package com.aishwarya.ers.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv;

public class ConnectionFactory {

    private static final Dotenv dotenv = Dotenv.load();

    private static final String URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USERNAME");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");

    // A static block runs once at runtime in case they forgot something in their .env or if its invalid
    static {
        if (URL == null || URL.isBlank()) {
            throw new IllegalStateException("Missing required .env value: DB_URL");
        }
        if (USER == null || USER.isBlank()) {
            throw new IllegalStateException("Missing required .env value: DB_USERNAME");
        }
        if (PASSWORD == null || PASSWORD.isBlank()) {
            throw new IllegalStateException("Missing required .env value: DB_PASSWORD");
        }
    }
    
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    
}
