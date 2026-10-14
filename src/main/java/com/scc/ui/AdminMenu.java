package com.scc;

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
            System.out.println("2. Manage Pending User Accounts");
            System.out.println("3. Logout");
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
                    managePendingUsers(scanner, userDao, adminUser.userId());
                    break;

                case 3:
                    System.out.println("Logging out from Admin session...");
                    userDao.logActivity(adminUser.userId(), "Admin logout: " + adminUser.username());
                    adminSession = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please pick between 1 and 3.");
            }
        }
    }

    private static void managePendingUsers(Scanner scanner, UserDao userDao, int adminId) {
        boolean managing = true;

        while (managing) {
            System.out.println("\n--- Pending User Accounts ---");
            List<User> pendingUsers = userDao.getPendingUsers();

            if (pendingUsers.isEmpty()) {
                System.out.println("No pending user accounts found.");
                return;
            }

            // Display pending users
            System.out.printf("%-5s | %-15s | %-10s | %-10s%n", "ID", "Username", "Role", "Status");
            System.out.println("--------------------------------------------------");
            for (User u : pendingUsers) {
                System.out.printf("%-5d | %-15s | %-10s | %-10s%n", u.userId(), u.username(), u.role(), u.status());
            }

            System.out.println("\nOptions:");
            System.out.println("1. Approve/Reject a User");
            System.out.println("2. Back to Admin Dashboard");
            System.out.print("Choose an option: ");
            int option = scanner.nextInt();
            scanner.nextLine();

            if (option == 1) {
                System.out.print("Enter User ID to update: ");
                int targetUserId = scanner.nextInt();
                scanner.nextLine();

                System.out.print("Set status to (ACTIVE / REJECTED): ");
                String newStatus = scanner.nextLine().toUpperCase();

                if (newStatus.equals("ACTIVE") || newStatus.equals("REJECTED")) {
                    if (userDao.updateUserStatus(targetUserId, newStatus)) {
                        System.out.println("User status successfully updated to " + newStatus + "!");
                        userDao.logActivity(adminId, "Updated user ID " + targetUserId + " status to " + newStatus);
                    } else {
                        System.out.println("Failed to update user status. Check if ID is correct.");
                    }
                } else {
                    System.out.println("Invalid status! Please type ACTIVE or REJECTED.");
                }
            } else if (option == 2) {
                managing = false;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }
}