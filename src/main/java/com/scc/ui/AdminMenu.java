
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.dao.ReservationDao;
import com.scc.dao.ActivityLogDao;

import com.scc.model.User;
import com.scc.model.ActivityLog;
import com.scc.model.Reservation;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    public static void showMenu(
            Scanner scanner,
            User admin,
            UserDao userDao) {

        ReservationDao reservationDao = new ReservationDao();

        boolean active = true;

        while (active) {

            System.out.println("\n================================");
            System.out.println("         ADMIN DASHBOARD");
            System.out.println("================================");
            System.out.println("Welcome, " + admin.username());
            System.out.println("--------------------------------");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending Users");
            System.out.println("3. Approve User");
            System.out.println("4. Update User Role/Status");
            System.out.println("5. View All Reservations");
            System.out.println("6. Update Reservation Status");
            System.out.println("7. View System Audit Logs");
            System.out.println("8. Logout");
            System.out.println("================================");

            int choice = Authentication.readInt(scanner, "Choose: ");

            switch (choice) {

                case 1 ->
                        displayUsers(userDao.getAllUsers());

                case 2 ->
                        displayUsers(userDao.getPendingUsers());

                case 3 -> {
                    int id = Authentication.readInt(
                            scanner, "User ID to approve: ");

                    if (userDao.approveUser(admin.userId(), id)) {
                        System.out.println("Account approved successfully.");
                    } else {
                        System.out.println("Approval failed.");
                    }
                }

                case 4 -> {
                    int id = Authentication.readInt(
                            scanner, "User ID: ");

                    System.out.print("New role (ADMIN/STAFF/RESIDENT): ");
                    String role = scanner.nextLine().trim().toUpperCase();

                    System.out.print("New status (PENDING/APPROVED/REJECTED): ");
                    String status = scanner.nextLine().trim().toUpperCase();

                    if (userDao.updateUserRoleAndStatus(
                            admin.userId(), id, role, status)) {

                        System.out.println("User updated successfully.");

                    } else {
                        System.out.println("Update failed.");
                    }
                }

                case 5 ->
                        displayReservations(reservationDao.getAllReservations());

                case 6 -> {
                    int id = Authentication.readInt(
                            scanner, "Reservation ID: ");

                    System.out.print(
                            "New status (APPROVED/REJECTED/CANCELLED): ");

                    String status = scanner.nextLine().trim().toUpperCase();

                    if (reservationDao.updateReservationStatus(
                            admin.userId(), id, status)) {

                        System.out.println("Reservation status updated successfully.");

                    } else {
                        System.out.println("Update failed.");
                    }
                }

                case 7 ->
                        viewAuditLogs(scanner);

                case 8 -> {
                    userDao.logout(admin);
                    active = false;
                    System.out.println("Admin logged out.");
                }

                default ->
                        System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // DISPLAY USERS
    private static void displayUsers(List<User> users) {

        String line =
                "+---------+----------------------+----------------------+----------+------------+";

        System.out.println("\n" + line);

        System.out.printf(
                "| %-7s | %-20s | %-20s | %-8s | %-10s |%n",
                "USER ID", "USERNAME", "PASSWORD", "ROLE", "STATUS"
        );

        System.out.println(line);

        if (users == null || users.isEmpty()) {

            System.out.printf(
                    "| %-7s | %-20s | %-20s | %-8s | %-10s |%n",
                    "", "No users found.", "", "", ""
            );

        } else {

            for (User user : users) {

                System.out.printf(
                        "| %-7d | %-20s | %-20s | %-8s | %-10s |%n",
                        user.userId(),
                        fit(user.username(), 20),
                        fit(user.password(), 20),
                        fit(user.role(), 8),
                        fit(user.status(), 10)
                );
            }
        }

        System.out.println(line);
    }

    // DISPLAY RESERVATIONS
    private static void displayReservations(
            List<Reservation> reservations) {

        String line =
                "+---------+----------+----------+----------------------+----------------------+------------+";

        System.out.println("\n" + line);

        System.out.printf(
                "| %-7s | %-8s | %-8s | %-20s | %-20s | %-10s |%n",
                "RES ID", "USER ID", "FAC ID",
                "DATE", "FACILITY", "STATUS"
        );

        System.out.println(line);

        if (reservations == null || reservations.isEmpty()) {

            System.out.printf(
                    "| %-7s | %-8s | %-8s | %-20s | %-20s | %-10s |%n",
                    "", "", "", "No reservations found.", "", ""
            );

        } else {

            for (Reservation reservation : reservations) {

                System.out.printf(
                        "| %-7d | %-8d | %-8d | %-20s | %-20s | %-10s |%n",
                        reservation.reservationId(),
                        reservation.userId(),
                        reservation.facilityId(),
                        fit(reservation.reservationDate(), 20),
                        fit(reservation.facilityName(), 20),
                        fit(reservation.status(), 10)
                );
            }
        }

        System.out.println(line);
    }

    // AUDIT LOG MENU
    private static void viewAuditLogs(Scanner scanner) {

        ActivityLogDao dao = new ActivityLogDao();

        boolean viewing = true;

        while (viewing) {

            System.out.println("\n================================");
            System.out.println("       SYSTEM AUDIT LOGS");
            System.out.println("================================");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Back");
            System.out.println("================================");

            int choice = Authentication.readInt(scanner, "Choose: ");

            switch (choice) {

                case 1 ->
                        displayLogs(dao.getAllLogs());

                case 2 -> {
                    int userId = Authentication.readInt(
                            scanner, "Enter User ID: ");

                    displayLogs(dao.getLogsByUserId(userId));
                }

                case 3 ->
                        viewing = false;

                default ->
                        System.out.println("Invalid choice.");
            }
        }
    }

    // DISPLAY AUDIT LOGS
    private static void displayLogs(List<ActivityLog> logs) {

        String line =
                "+-------+----------+----------------------+-----------------------------------------------+----------------------+";

        System.out.println("\n" + line);

        System.out.printf(
                "| %-5s | %-8s | %-20s | %-45s | %-20s |%n",
                "ID", "USER ID", "ACTION TYPE",
                "DESCRIPTION", "TIMESTAMP"
        );

        System.out.println(line);

        if (logs == null || logs.isEmpty()) {

            System.out.printf(
                    "| %-5s | %-8s | %-20s | %-45s | %-20s |%n",
                    "", "", "", "No audit logs found.", ""
            );

        } else {

            for (ActivityLog log : logs) {

                String userId = log.userId() == null
                        ? "N/A"
                        : String.valueOf(log.userId());

                System.out.printf(
                        "| %-5d | %-8s | %-20s | %-45s | %-20s |%n",
                        log.logId(),
                        fit(userId, 8),
                        fit(log.actionType(), 20),
                        fit(log.description(), 45),
                        fit(log.timestamp(), 20)
                );
            }
        }

        System.out.println(line);
    }

    // LIMIT TEXT LENGTH
    private static String fit(String value, int width) {

        if (value == null) {
            return "";
        }

        if (value.length() > width) {
            return value.substring(0, width - 3) + "...";
        }

        return value;
    }
}
