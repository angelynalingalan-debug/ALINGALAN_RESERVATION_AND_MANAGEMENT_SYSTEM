
package com.scc.dao;

import com.scc.model.Reservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDao {

    private final ActivityLogDao logDao = new ActivityLogDao();

    // CREATE RESERVATION
    public boolean createReservation(
            int userId,
            int facilityId,
            String date,
            String purpose) {

        String sql = """
            INSERT INTO reservations
            (user_id, facility_id, reservation_date, purpose, status)
            VALUES (?, ?, ?, ?, 'PENDING')
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.setInt(2, facilityId);
            ps.setString(3, date);
            ps.setString(4, purpose);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                int reservationId = -1;

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        reservationId = keys.getInt(1);
                    }
                }

                logDao.logAction(
                        userId,
                        "RESERVATION_CREATED",
                        "User ID " + userId
                                + " created Reservation ID " + reservationId
                                + " for Facility ID " + facilityId
                );

                System.out.println("Reservation created successfully.");
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Reservation error: " + e.getMessage());
        }

        return false;
    }

    // GET ALL RESERVATIONS
    public List<Reservation> getAllReservations() {
        return getReservations(0);
    }

    // GET RESERVATIONS BY USER
    public List<Reservation> getReservationsByUserId(int userId) {
        return getReservations(userId);
    }

    // COMMON RESERVATION RETRIEVAL
    private List<Reservation> getReservations(int userId) {

        List<Reservation> reservations = new ArrayList<>();

        String sql = """
            SELECT r.*, u.username, f.facility_name
            FROM reservations r
            JOIN users u ON r.user_id = u.user_id
            JOIN facilities f ON r.facility_id = f.facility_id
        """;

        if (userId != 0) {
            sql += " WHERE r.user_id = ?";
        }

        sql += " ORDER BY r.reservation_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (userId != 0) {
                ps.setInt(1, userId);
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    reservations.add(new Reservation(
                            rs.getInt("reservation_id"),
                            rs.getInt("user_id"),
                            rs.getInt("facility_id"),
                            rs.getString("reservation_date"),
                            rs.getString("purpose"),
                            rs.getString("status"),
                            rs.getString("username"),
                            rs.getString("facility_name")
                    ));
                }
            }

        } catch (SQLException e) {
            System.out.println("Reservation retrieval error: " + e.getMessage());
        }

        return reservations;
    }

    // UPDATE RESERVATION STATUS
    public boolean updateReservationStatus(
            int actorId,
            int reservationId,
            String newStatus) {

        if (newStatus == null) {
            System.out.println("Status is required.");
            return false;
        }

        newStatus = newStatus.trim().toUpperCase();

        if (!List.of("APPROVED", "REJECTED", "CANCELLED").contains(newStatus)) {
            System.out.println("Invalid reservation status.");
            return false;
        }

        String oldStatus = null;

        String selectSql = """
            SELECT status
            FROM reservations
            WHERE reservation_id = ?
        """;

        String updateSql = """
            UPDATE reservations
            SET status = ?
            WHERE reservation_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection()) {

            // GET PREVIOUS STATUS
            try (PreparedStatement selectPs = conn.prepareStatement(selectSql)) {

                selectPs.setInt(1, reservationId);

                try (ResultSet rs = selectPs.executeQuery()) {
                    if (rs.next()) {
                        oldStatus = rs.getString("status");
                    } else {
                        System.out.println("Reservation not found.");
                        return false;
                    }
                }
            }

            // UPDATE STATUS
            try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {

                updatePs.setString(1, newStatus);
                updatePs.setInt(2, reservationId);

                if (updatePs.executeUpdate() > 0) {

                    String actionType;

                    if ("REJECTED".equals(newStatus)) {
                        actionType = "BOOKING_REJECTED";
                    } else if (!newStatus.equals(oldStatus)) {
                        actionType = "RESERVATION_STATUS_EDITED";
                    } else {
                        actionType = "RESERVATION_UPDATED";
                    }

                    logDao.logAction(
                            actorId,
                            actionType,
                            "Reservation ID " + reservationId
                                    + " status changed from " + oldStatus
                                    + " to " + newStatus
                    );

                    System.out.println("Reservation status updated successfully.");
                    return true;
                }
            }

        } catch (SQLException e) {
            System.out.println("Reservation update error: " + e.getMessage());
        }

        return false;
    }
}
