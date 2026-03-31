package com.jdbc.minidishdb.repository;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class DataSource {

    private final Dotenv dotenv = Dotenv.load();

    private final String url = dotenv.get("JDBC_URL");
    private final String username = dotenv.get("USERNAME");
    private final String password = dotenv.get("PASSWORD");

    public Connection getDBConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de connexion : " + e.getMessage());
        }
    }
}

