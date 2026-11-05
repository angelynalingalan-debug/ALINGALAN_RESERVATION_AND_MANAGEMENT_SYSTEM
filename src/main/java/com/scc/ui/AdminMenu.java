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
            System.out.println("4. Update User Role and Status");
            System.out.println("5. Logout");

            int choice = readIntInput(scanner, "Choose an option: ");

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
                    updateUserRoleAndStatusMenu(scanner, userDao, adminUser);
                    break;

                case 5:
                    System.out.println("Logging out from Admin session...");
                    userDao.logActivity(adminUser.userId(), "Admin logout: " + adminUser.username());
                    adminSession = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please pick between 1 and 5.");
            }
        }
    }

    // --- INPUT VALIDATION WRAPPERS ---

    // Catches non-numeric inputs (letters/symbols) without throwing InputMismatchException
    private static int readIntInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid number.");
            }
        }
    }

    // Repeatedly prompts until a valid role (or cancellation '0') is provided
    private static String readValidRole(Scanner scanner) {
        while (true) {
            System.out.print("Enter new role (ADMIN / STAFF / RESIDENT) (Enter 0 to cancel): ");
            String role = scanner.nextLine().trim().toUpperCase();

            if (role.equals("0") || role.equals("ADMIN") || role.equals("STAFF") || role.equals("RESIDENT")) {
                return role;
            }
            System.out.println("Invalid role! Accepted roles are ADMIN, STAFF, or RESIDENT.");
        }
    }

    // Repeatedly prompts until a valid status (or cancellation '0') is provided
    private static String readValidStatus(Scanner scanner) {
        while (true) {
            System.out.print("Enter new status (APPROVED / PENDING) (Enter 0 to cancel): ");
            String status = scanner.nextLine().trim().toUpperCase();

            if (status.equals("0") || status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }
            System.out.println("Invalid status! Accepted statuses are APPROVED or PENDING.");
        }
    }

    // --- TABLE RENDERING HELPER ---

    private static void renderUserTable(List<User> users) {
        String format = "%-5s | %-15s | %-12s | %-10s%n";
        String border = "--------------------------------------------------------";

        System.out.printf(format, "ID", "Username", "Role", "Status");
        System.out.println(border);
        for (User u : users) {
            System.out.printf(format, u.userId(), u.username(), u.role(), u.status());
        }
    }

    // --- DASHBOARD ACTIONS ---

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

            int option = readIntInput(scanner, "Choose an option: ");

            if (option == 1) {
                int targetUserId = readIntInput(scanner, "Enter Target User ID to approve (Enter 0 to cancel): ");

                if (targetUserId == 0) {
                    System.out.println("Approval action cancelled.");
                    continue;
                }

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

    private static void updateUserRoleAndStatusMenu(Scanner scanner, UserDao userDao, User adminUser) {
        boolean updating = true;

        while (updating) {
            System.out.println("\n--- Update User Role and Status ---");
            List<User> allUsers = userDao.getAllUsers();

            if (allUsers.isEmpty()) {
                System.out.println("No users found in the system.");
                return;
            }

            renderUserTable(allUsers);

            int targetUserId = readIntInput(scanner, "\nEnter Target User ID to update (Enter 0 to cancel): ");

            if (targetUserId == 0) {
                System.out.println("Update action cancelled.");
                return;
            }

            if (targetUserId == adminUser.userId()) {
                System.out.println("WARNING: Modifying the currently active admin account is restricted.");
                continue;
            }

            boolean userExists = allUsers.stream().anyMatch(u -> u.userId() == targetUserId);
            if (!userExists) {
                System.out.println("Invalid User ID! User not found.");
                continue;
            }

            // Using the validation wrappers
            String newRole = readValidRole(scanner);
            if (newRole.equals("0")) {
                System.out.println("Update action cancelled.");
                return;
            }

            String newStatus = readValidStatus(scanner);
            if (newStatus.equals("0")) {
                System.out.println("Update action cancelled.");
                return;
            }

            if (userDao.updateUserRoleAndStatus(targetUserId, newRole, newStatus)) {
                System.out.println("User ID " + targetUserId + " successfully updated!");
                userDao.logActivity(adminUser.userId(), "Updated role and status for user ID " + targetUserId);
            } else {
                System.out.println("Failed to update user in the database.");
            }

            System.out.print("\nDo you want to update another user? (y/n): ");
            String continueChoice = scanner.nextLine().trim();
            if (!continueChoice.equalsIgnoreCase("y")) {
                updating = false;
            }
        }
    }
}