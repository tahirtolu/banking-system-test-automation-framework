package com.banking.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Configuration
public class DatabaseConfig {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void enableForeignKeys() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            // SQLite için foreign key desteğini etkinleştir
            statement.execute("PRAGMA foreign_keys = ON;");
            System.out.println("✓ SQLite foreign keys enabled");
        } catch (Exception e) {
            System.err.println("⚠ Failed to enable SQLite foreign keys: " + e.getMessage());
        }
    }
}

