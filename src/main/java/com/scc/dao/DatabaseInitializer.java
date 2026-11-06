
package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initializeDatabase() {

        try (Connection conn = DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement()) {

                // USERS TABLE
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT UNIQUE NOT NULL,
                        password TEXT NOT NULL,
                        role TEXT NOT NULL
                            CHECK(role IN ('ADMIN','STAFF','RESIDENT')),
                        status TEXT NOT NULL DEFAULT 'PENDING'
                            CHECK(status IN ('PENDING','APPROVED','REJECTED'))
                    )
                """);

                // FACILITIES TABLE
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS facilities (
                        facility_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        facility_name TEXT UNIQUE NOT NULL,
                        location TEXT NOT NULL,
                        availability TEXT DEFAULT 'AVAILABLE'
                    )
                """);

                // RESERVATIONS TABLE
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS reservations (
                        reservation_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        facility_id INTEGER NOT NULL,
                        reservation_date TEXT NOT NULL,
                        purpose TEXT NOT NULL,
                        status TEXT DEFAULT 'PENDING',
                        FOREIGN KEY(user_id)
                            REFERENCES users(user_id),
                        FOREIGN KEY(facility_id)
                            REFERENCES facilities(facility_id)
                    )
                """);

                // ACTIVITY LOGS TABLE
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS activity_logs (
                        log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER,
                        action_type TEXT NOT NULL,
                        description TEXT NOT NULL,
                        timestamp TEXT DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(user_id)
                            REFERENCES users(user_id)
                            ON DELETE CASCADE
                    )
                """);
            }

            // ENSURE RESERVATION COLUMNS
            ensureColumn(
                    conn, "reservations",
                    "user_id", "INTEGER NOT NULL DEFAULT 0"
            );

            ensureColumn(
                    conn, "reservations",
                    "facility_id", "INTEGER NOT NULL DEFAULT 0"
            );

            ensureColumn(
                    conn, "reservations",
                    "reservation_date", "TEXT NOT NULL DEFAULT ''"
            );

            ensureColumn(
                    conn, "reservations",
                    "purpose", "TEXT NOT NULL DEFAULT ''"
            );

            ensureColumn(
                    conn, "reservations",
                    "status", "TEXT DEFAULT 'PENDING'"
            );

            // CHECK AND FIX ACTIVITY LOGS FOREIGN KEY
            ensureActivityLogsCascade(conn);

            // REMOVE DUPLICATE FACILITIES
            removeDuplicateFacilities(conn);

            // PREVENT FUTURE FACILITY DUPLICATES
            try (Statement stmt = conn.createStatement()) {

                stmt.execute("""
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    idx_unique_facility_name
                    ON facilities (LOWER(TRIM(facility_name)))
                """);
            }

            // CREATE DEFAULT ADMIN
            seedAdmin(conn);

            // CREATE DEFAULT FACILITIES
            seedFacilities(conn);

            conn.commit();

            System.out.println(
                    "Database initialized successfully."
            );

        } catch (SQLException e) {

            System.out.println(
                    "Database initialization error: " + e.getMessage()
            );
        }
    }

    // CHECK IF COLUMN EXISTS
    private static void ensureColumn(
            Connection conn,
            String tableName,
            String columnName,
            String columnDefinition
    ) throws SQLException {

        boolean exists = false;

        String sql = "PRAGMA table_info(" + tableName + ")";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                String existingColumn = rs.getString("name");

                if (columnName.equalsIgnoreCase(existingColumn)) {
                    exists = true;
                    break;
                }
            }
        }

        if (!exists) {

            String alterSql =
                    "ALTER TABLE " + tableName +
                            " ADD COLUMN " + columnName +
                            " " + columnDefinition;

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(alterSql);
            }

            System.out.println(
                    "Database updated: Added " + columnName + " column."
            );
        }
    }

    // ENSURE ACTIVITY LOGS USE ON DELETE CASCADE
    private static void ensureActivityLogsCascade(
            Connection conn
    ) throws SQLException {

        boolean cascadeExists = false;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "PRAGMA foreign_key_list(activity_logs)")) {

            while (rs.next()) {

                String table = rs.getString("table");
                String from = rs.getString("from");
                String to = rs.getString("to");
                String onDelete = rs.getString("on_delete");

                if ("users".equalsIgnoreCase(table)
                        && "user_id".equalsIgnoreCase(from)
                        && "user_id".equalsIgnoreCase(to)
                        && "CASCADE".equalsIgnoreCase(onDelete)) {

                    cascadeExists = true;
                    break;
                }
            }
        }

        if (cascadeExists) {
            System.out.println(
                    "Activity logs foreign key already uses CASCADE."
            );
            return;
        }

        System.out.println(
                "Updating activity_logs foreign key..."
        );

        // CREATE A NEW TABLE WITH CASCADE
        try (Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE activity_logs_new (
                    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    action_type TEXT NOT NULL,
                    description TEXT NOT NULL,
                    timestamp TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY(user_id)
                        REFERENCES users(user_id)
                        ON DELETE CASCADE
                )
            """);
        }

        // COPY EXISTING RECORDS
        try (Statement stmt = conn.createStatement()) {

            stmt.execute("""
                INSERT INTO activity_logs_new
                (log_id, user_id, action_type, description, timestamp)
                SELECT
                    log_id,
                    user_id,
                    action_type,
                    description,
                    timestamp
                FROM activity_logs
            """);
        }

        // REPLACE OLD TABLE
        try (Statement stmt = conn.createStatement()) {

            stmt.execute("DROP TABLE activity_logs");

            stmt.execute("""
                ALTER TABLE activity_logs_new
                RENAME TO activity_logs
            """);
        }

        System.out.println(
                "Activity logs foreign key updated successfully."
        );
    }

    // REMOVE DUPLICATE FACILITIES
    private static void removeDuplicateFacilities(
            Connection conn
    ) throws SQLException {

        String updateSql = """
            UPDATE reservations
            SET facility_id = (
                SELECT MIN(f2.facility_id)
                FROM facilities f2
                WHERE LOWER(TRIM(f2.facility_name)) = (
                    SELECT LOWER(TRIM(f3.facility_name))
                    FROM facilities f3
                    WHERE f3.facility_id = reservations.facility_id
                )
            )
            WHERE facility_id IN (
                SELECT facility_id
                FROM facilities
                WHERE LOWER(TRIM(facility_name)) IN (
                    SELECT LOWER(TRIM(facility_name))
                    FROM facilities
                    GROUP BY LOWER(TRIM(facility_name))
                    HAVING COUNT(*) > 1
                )
            )
        """;

        try (PreparedStatement ps =
                     conn.prepareStatement(updateSql)) {

            ps.executeUpdate();
        }

        String deleteSql = """
            DELETE FROM facilities
            WHERE facility_id NOT IN (
                SELECT MIN(facility_id)
                FROM facilities
                GROUP BY LOWER(TRIM(facility_name))
            )
        """;

        try (PreparedStatement ps =
                     conn.prepareStatement(deleteSql)) {

            ps.executeUpdate();
        }
    }

    // CREATE DEFAULT ADMIN
    private static void seedAdmin(
            Connection conn
    ) throws SQLException {

        String sql = """
            INSERT OR IGNORE INTO users
            (username, password, role, status)
            VALUES (?, ?, 'ADMIN', 'APPROVED')
        """;

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(1, "admin");
            ps.setString(2, "admin123");

            ps.executeUpdate();
        }
    }

    // CREATE DEFAULT FACILITIES
    private static void seedFacilities(
            Connection conn
    ) throws SQLException {

        String sql = """
            INSERT OR IGNORE INTO facilities
            (facility_name, location, availability)
            VALUES (?, ?, 'AVAILABLE')
        """;

        String[][] facilities = {
                {"Community Hall", "Main Building"},
                {"Basketball Court", "Outdoor Area"},
                {"Function Room", "Second Floor"}
        };

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            for (String[] facility : facilities) {

                ps.setString(1, facility[0]);
                ps.setString(2, facility[1]);

                ps.executeUpdate();
            }
        }
    }
}
