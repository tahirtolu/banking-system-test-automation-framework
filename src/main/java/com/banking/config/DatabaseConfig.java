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
                    // Enable Foreign Keys
                    statement.execute("PRAGMA foreign_keys = ON;");

                    // Enable Write-Ahead Logging (WAL) for concurrency (Readers don't block
                    // Writers)
                    statement.execute("PRAGMA journal_mode = WAL;");

                    // Set synchronous to NORMAL for better performance in WAL mode
                    statement.execute("PRAGMA synchronous = NORMAL;");

                    System.out.println("✓ SQLite optimization enabled (WAL + Foreign Keys)");
                }
            } else {
                System.out.println("✓ Database: " + databaseProductName + " (foreign keys handled by database)");
            }
        } catch (Exception e) {
            System.err.println("⚠ Failed to enable SQLite foreign keys: " + e.getMessage());
        }
    }
}
