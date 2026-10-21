package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    public static void showMenu(Scanner scanner, User adminUser, UserDao userDao) {
        boolean adminSession = true;

        while (adminSession) {
            System.out.println("\n=== ADMIN DASHBOARD ===");
            System.out.println("1. View Profile Details");
            System.out.println("2. View All System Users");
            System.out.println("3. Manage Pending User Accounts & Approvals");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    System.out.println("\n-- Admin Profile --");
                    System.out.println("ID: " + adminUser.userId());
                    System.out.println("Username: " + adminUser.username());
                    System.out.println("Role: " + adminUser.role());
                    System.out.println("Status: " + adminUser.status());
                    break;

                case 2:
                    viewAllUsers(userDao);
                    break;

                case 3:
                    managePendingUsers(scanner, userDao, adminUser.userId());
                    break;

                case 4:
                    System.out.println("Logging out from Admin session...");
                    userDao.logActivity(adminUser.userId(), "Admin logout: " + adminUser.username());
                    adminSession = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please pick between 1 and 4.");
            }
        }
    }

    // Structured table rendering helper method using the specified string formatters
    private static void renderUserTable(List<User> users) {
        String format = "%-5s | %-15s | %-12s | %-10s%n";
        String border = "--------------------------------------------------------";

        System.out.printf(format, "ID", "Username", "Role", "Status");
        System.out.println(border);
        for (User u : users) {
            System.out.printf(format, u.userId(), u.username(), u.role(), u.status());
        }
    }

    private static void viewAllUsers(UserDao userDao) {
        System.out.println("\n--- All System Users ---");
        List<User> allUsers = userDao.getAllUsers();

        if (allUsers.isEmpty()) {
            System.out.println("No users found in the system.");
            return;
        }

        renderUserTable(allUsers);
    }

    private static void managePendingUsers(Scanner scanner, UserDao userDao, int adminId) {
        boolean managing = true;

        while (managing) {
            System.out.println("\n--- Pending User Accounts ---");
            List<User> pendingUsers = userDao.getPendingUsers();

            if (pendingUsers.isEmpty()) {
                System.out.println("No pending user accounts awaiting approval.");
                return;
            }

            renderUserTable(pendingUsers);

            System.out.println("\nOptions:");
            System.out.println("1. Approve a User Account");
            System.out.println("2. Back to Admin Dashboard");
            System.out.print("Choose an option: ");
            int option = scanner.nextInt();
            scanner.nextLine();

            if (option == 1) {
                System.out.print("Enter Target User ID to approve (Enter 0 to cancel): ");
                int targetUserId = scanner.nextInt();
                scanner.nextLine();

                // Input safety and cancellation check
                if (targetUserId == 0) {
                    System.out.println("Approval action cancelled.");
                    continue;
                }

                // Verify the ID exists in the pending list
                boolean existsInPending = pendingUsers.stream()
                        .anyMatch(u -> u.userId() == targetUserId);

                if (existsInPending) {
                    if (userDao.approveUser(targetUserId)) {
                        System.out.println("User account ID " + targetUserId + " successfully approved!");
                        userDao.logActivity(adminId, "Approved user account ID " + targetUserId);
                    } else {
                        System.out.println("Failed to update user status in the database.");
                    }
                } else {
                    System.out.println("Invalid User ID! ID was not found in the pending list.");
                }
            } else if (option == 2) {
                managing = false;
            } else {
                System.out.println("Invalid choice. Please pick 1 or 2.");
            }
        }
    }
}