package com.scc.dao;

import com.scc.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private final ActivityLogDao logDao = new ActivityLogDao();

    // REGISTER ACCOUNT
    public boolean register(String username, String password, String role) {

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            System.out.println("Username and password are required.");
            return false;
        }

        if (!"STAFF".equalsIgnoreCase(role)
                && !"RESIDENT".equalsIgnoreCase(role)) {
            System.out.println("Invalid registration role.");
            return false;
        }

        String sql = """
            INSERT INTO users (username, password, role, status)
            VALUES (?, ?, ?, 'PENDING')
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username.trim());
            ps.setString(2, password);
            ps.setString(3, role.toUpperCase());

            int rows = ps.executeUpdate();

            if (rows > 0) {

                int userId = -1;

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        userId = keys.getInt(1);
                    }
                }

                if (userId > 0) {
                    logDao.logAction(
                            userId,
                            "ACCOUNT_REGISTERED",
                            "New " + role.toUpperCase()
                                    + " account registered: " + username.trim()
                    );
                }

                System.out.println(
                        "Registration successful. Waiting for admin approval."
                );

                return true;
            }

        } catch (SQLException e) {
            System.out.println("Registration error: " + e.getMessage());
        }

        return false;
    }

    // LOGIN
    public User login(String username, String password) {

        String sql = """
            SELECT user_id, username, password, role, status
            FROM users
            WHERE username = ? AND password = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    User user = new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("status")
                    );

                    if (!"APPROVED".equalsIgnoreCase(user.status())) {

                        logDao.logAction(
                                user.userId(),
                                "LOGIN_BLOCKED",
                                "Login blocked because account is "
                                        + user.status() + ": " + user.username()
                        );

                        System.out.println(
                                "Your account is not approved."
                        );

                        return null;
                    }

                    logDao.logAction(
                            user.userId(),
                            "LOGIN",
                            "User logged in: " + user.username()
                    );

                    return user;
                }
            }

        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
            return null;
        }

        // LOG FAILED LOGIN
        logDao.logAction(
                null,
                "LOGIN_FAILED",
                "Failed login attempt for username: " + username
        );

        System.out.println("Invalid username or password.");
        return null;
    }

    // LOGOUT
    public void logout(User user) {

        if (user != null) {
            logDao.logAction(
                    user.userId(),
                    "LOGOUT",
                    "User logged out: " + user.username()
            );
        }
    }

    // GET ALL USERS
    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();

        String sql = """
            SELECT user_id, username, password, role, status
            FROM users
            ORDER BY user_id
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("status")
                ));
            }

        } catch (SQLException e) {
            System.out.println("User retrieval error: " + e.getMessage());
        }

        return users;
    }

    // GET PENDING USERS
    public List<User> getPendingUsers() {

        List<User> users = new ArrayList<>();

        String sql = """
            SELECT user_id, username, password, role, status
            FROM users
            WHERE status = 'PENDING'
            ORDER BY user_id
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("status")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Pending users retrieval error: " + e.getMessage());
        }

        return users;
    }

    // APPROVE USER
    public boolean approveUser(int adminId, int userId) {

        String sql = """
            UPDATE users
            SET status = 'APPROVED'
            WHERE user_id = ? AND status = 'PENDING'
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                logDao.logAction(
                        adminId,
                        "USER_APPROVED",
                        "Admin approved user ID: " + userId
                );

                return true;
            }

        } catch (SQLException e) {
            System.out.println("Approval error: " + e.getMessage());
        }

        return false;
    }

    // UPDATE USER ROLE AND STATUS
    public boolean updateUserRoleAndStatus(
            int adminId,
            int userId,
            String role,
            String status) {

        if (role == null || status == null) {
            System.out.println("Role and status are required.");
            return false;
        }

        role = role.toUpperCase();
        status = status.toUpperCase();

        if (!role.equals("ADMIN")
                && !role.equals("STAFF")
                && !role.equals("RESIDENT")) {

            System.out.println("Invalid role.");
            return false;
        }

        if (!status.equals("PENDING")
                && !status.equals("APPROVED")
                && !status.equals("REJECTED")) {

            System.out.println("Invalid status.");
            return false;
        }

        String sql = """
            UPDATE users
            SET role = ?, status = ?
            WHERE user_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role);
            ps.setString(2, status);
            ps.setInt(3, userId);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                logDao.logAction(
                        adminId,
                        "USER_UPDATED",
                        "Admin updated user ID " + userId
                                + " to role " + role
                                + " and status " + status
                );

                return true;
            }

        } catch (SQLException e) {
            System.out.println("User update error: " + e.getMessage());
        }

        return false;
    }
}
