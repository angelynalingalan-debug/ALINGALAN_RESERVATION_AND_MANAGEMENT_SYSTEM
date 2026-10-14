package com.scc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:ManagementRecordsDatabase.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Drop existing tables during development to prevent column mismatch errors
            stmt.execute("DROP TABLE IF EXISTS activity_logs;");
            stmt.execute("DROP TABLE IF EXISTS reservations;");
            stmt.execute("DROP TABLE IF EXISTS facilities;");
            stmt.execute("DROP TABLE IF EXISTS users;");

            // 1. Users Table
            stmt.execute("CREATE TABLE users (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "role TEXT NOT NULL, " +
                    "status TEXT DEFAULT 'PENDING');");

            // 2. Facilities Table
            stmt.execute("CREATE TABLE facilities (" +
                    "facility_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "facility_name TEXT NOT NULL, " +
                    "location TEXT, " +
                    "availability TEXT DEFAULT 'AVAILABLE');");

            // 3. Reservations Table
            stmt.execute("CREATE TABLE reservations (" +
                    "reservation_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "facility_id INTEGER, " +
                    "reservation_date TEXT, " +
                    "status TEXT DEFAULT 'PENDING', " +
                    "FOREIGN KEY(user_id) REFERENCES users(user_id), " +
                    "FOREIGN KEY(facility_id) REFERENCES facilities(facility_id));");

            // 4. Activity Logs Table
            stmt.execute("CREATE TABLE activity_logs (" +
                    "log_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "action TEXT NOT NULL, " +
                    "date_time TEXT DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(user_id) REFERENCES users(user_id));");

        } catch (SQLException e) {
            System.out.println("Database initialization error: " + e.getMessage());
        }
    }
}