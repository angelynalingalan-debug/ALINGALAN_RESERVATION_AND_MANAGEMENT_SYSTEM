package com.scc.dao;

import com.scc.model.ActivityLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    // Insert audit log. Nullable userId is allowed for failed login.
    public void logAction(Integer userId, String actionType, String description) {

        String sql = """
            INSERT INTO activity_logs
            (user_id, action_type, description)
            VALUES (?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (userId == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, userId);
            }

            ps.setString(2, actionType);
            ps.setString(3, description);

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Log error: " + e.getMessage());
        }
    }

    // Get all audit logs
    public List<ActivityLog> getAllLogs() {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
            SELECT * FROM activity_logs
            ORDER BY timestamp DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                logs.add(mapLog(rs));
            }

        } catch (SQLException e) {
            System.out.println("Log retrieval error: " + e.getMessage());
        }

        return logs;
    }

    // Get audit logs by user ID
    public List<ActivityLog> getLogsByUserId(int userId) {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
            SELECT * FROM activity_logs
            WHERE user_id = ?
            ORDER BY timestamp DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapLog(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Log retrieval error: " + e.getMessage());
        }

        return logs;
    }

    private ActivityLog mapLog(ResultSet rs) throws SQLException {

        int id = rs.getInt("user_id");
        Integer userId = rs.wasNull() ? null : id;

        return new ActivityLog(
                rs.getInt("log_id"),
                userId,
                rs.getString("action_type"),
                rs.getString("description"),
                rs.getString("timestamp")
        );
    }
}
