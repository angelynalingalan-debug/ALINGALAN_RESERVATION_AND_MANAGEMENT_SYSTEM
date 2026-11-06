
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class Authentication {

    // START MAIN MENU
    public static void start(
            Scanner scanner,
            UserDao userDao) {

        boolean running = true;

        while (running) {

            System.out.println("\n================================");
            System.out.println("           MAIN MENU");
            System.out.println("================================");
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.println("================================");

            int choice = readInt(scanner, "Enter your choice: ");

            switch (choice) {

                // REGISTER ACCOUNT
                case 1 -> registerAccount(scanner, userDao);

                // LOGIN
                case 2 -> loginAccount(scanner, userDao);

                // EXIT
                case 3 -> {
                    running = false;
                    System.out.println("\nThank you for using the system!");
                    System.out.println("Goodbye!");
                }

                default -> System.out.println(
                        "Invalid choice. Please select 1-3.");
            }
        }
    }

    // REGISTER ACCOUNT
    private static void registerAccount(
            Scanner scanner,
            UserDao userDao) {

        System.out.println("\n================================");
        System.out.println("        REGISTER ACCOUNT");
        System.out.println("================================");

        System.out.print("Enter Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println(
                    "Username and password cannot be empty.");
            return;
        }

        System.out.println("\nSELECT ACCOUNT ROLE");
        System.out.println("1. STAFF");
        System.out.println("2. RESIDENT");

        int roleChoice = readInt(scanner, "Choose role: ");

        String role;

        switch (roleChoice) {
            case 1 -> role = "STAFF";
            case 2 -> role = "RESIDENT";
            default -> {
                System.out.println("Invalid role selection.");
                return;
            }
        }

        boolean registered = userDao.register(
                username,
                password,
                role
        );

        if (registered) {

            System.out.println("\nRegistration successful!");

            System.out.println("Username: " + username);
            System.out.println("Role: " + role);
            System.out.println("Account Status: PENDING");

            System.out.println(
                    "Please wait for administrator approval.");

        } else {

            System.out.println("\nRegistration failed.");

            System.out.println(
                    "Username may already exist or a database error occurred.");
        }
    }

    // LOGIN ACCOUNT
    private static void loginAccount(
            Scanner scanner,
            UserDao userDao) {

        System.out.println("\n================================");
        System.out.println("              LOGIN");
        System.out.println("================================");

        System.out.print("Enter Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        if (username.isEmpty() || password.isEmpty()) {

            System.out.println(
                    "Username and password are required.");

            return;
        }

        User user = userDao.login(username, password);

        if (user == null) {

            System.out.println(
                    "\nLogin failed or account is not approved.");

            return;
        }

        System.out.println("\nLogin successful!");
        System.out.println("Welcome, " + user.username());
        System.out.println("Role: " + user.role());

        // OPEN DASHBOARD BASED ON ROLE
        switch (user.role().toUpperCase()) {

            case "ADMIN" -> AdminMenu.showMenu(
                    scanner,
                    user,
                    userDao
            );

            case "STAFF" -> StaffMenu.showMenu(
                    scanner,
                    user,
                    userDao
            );

            case "RESIDENT" -> ResidentMenu.showMenu(
                    scanner,
                    user,
                    userDao
            );

            default -> System.out.println(
                    "Invalid user role.");
        }
    }

    // INTEGER INPUT VALIDATION
    public static int readInt(
            Scanner scanner,
            String message) {

        while (true) {

            System.out.print(message);

            String input = scanner.nextLine().trim();

            try {

                return Integer.parseInt(input);

            } catch (NumberFormatException e) {

                System.out.println(
                        "Invalid input. Please enter a number.");
            }
        }
    }
}
