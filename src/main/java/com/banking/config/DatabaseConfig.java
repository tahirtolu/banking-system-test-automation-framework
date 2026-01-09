package com.banking.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;

@Configuration
public class DatabaseConfig {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void enableForeignKeys() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName().toLowerCase();
            
            // Sadece SQLite için PRAGMA foreign_keys = ON; çalıştır
            if (databaseProductName.contains("sqlite")) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("PRAGMA foreign_keys = ON;");
                    System.out.println("✓ SQLite foreign keys enabled");
                }
            } else {
                System.out.println("✓ Database: " + databaseProductName + " (foreign keys handled by database)");
            }
        } catch (Exception e) {
            System.err.println("⚠ Failed to enable SQLite foreign keys: " + e.getMessage());
        }
    }
}

