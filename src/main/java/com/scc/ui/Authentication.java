package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class Authentication {
    public static void start(Scanner scanner, UserDao userDao) {
        boolean running = true;
        while (running) {
            System.out.println("\n[MAIN MENU]");
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume leftover newline

            switch (choice) {
                case 1:
                    handleRegistration(scanner, userDao);
                    break;
                case 2:
                    handleLogin(scanner, userDao);
                    break;
                case 3:
                    System.out.println("Exiting system. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please select 1, 2, or 3.");
            }
        }
    }

    private static void handleRegistration(Scanner scanner, UserDao userDao) {
        System.out.print("Enter username: ");
        String regUser = scanner.nextLine();
        System.out.print("Enter password: ");
        String regPass = scanner.nextLine();
        System.out.print("Enter role (ADMIN / STAFF / RESIDENT): ");
        String regRole = scanner.nextLine().toUpperCase();

        if (!regRole.equals("ADMIN") && !regRole.equals("STAFF") && !regRole.equals("RESIDENT")) {
            System.out.println("Invalid role! Please choose ADMIN, STAFF, or RESIDENT.");
            return;
        }

        if (userDao.registerUser(regUser, regPass, regRole)) {
            System.out.println("Registration successful! Account status is PENDING.");
        } else {
            System.out.println("Registration failed. Username may already exist.");
        }
    }

    private static void handleLogin(Scanner scanner, UserDao userDao) {
        System.out.print("Enter username: ");
        String loginUser = scanner.nextLine();
        System.out.print("Enter password: ");
        String loginPass = scanner.nextLine();

        User user = userDao.login(loginUser, loginPass);
        if (user != null) {
            if (user.status().equals("PENDING") && !user.role().equalsIgnoreCase("ADMIN")) {
                System.out.println("\nYour account is still PENDING approval by an administrator.");
                return;
            }

            System.out.println("\nLogin successful! Welcome, " + user.username() + " (" + user.role() + ")");
            userDao.logActivity(user.userId(), "User login: " + user.username());

            if (user.role().equalsIgnoreCase("ADMIN")) {
                AdminMenu.showMenu(scanner, user, userDao);
            } else {
                boolean loggedIn = true;
                while (loggedIn) {
                    System.out.println("\n=== " + user.role() + " DASHBOARD ===");
                    System.out.println("1. View Profile Details");
                    System.out.println("2. Logout");
                    System.out.print("Choose an option: ");
                    int subChoice = scanner.nextInt();
                    scanner.nextLine();

                    switch (subChoice) {
                        case 1:
                            System.out.println("\n-- User Profile --");
                            System.out.println("ID: " + user.userId());
                            System.out.println("Username: " + user.username());
                            System.out.println("Role: " + user.role());
                            System.out.println("Status: " + user.status());
                            break;
                        case 2:
                            System.out.println("Logging out...");
                            userDao.logActivity(user.userId(), "User logout: " + user.username());
                            loggedIn = false;
                            break;
                        default:
                            System.out.println("Invalid choice. Please pick 1 or 2.");
                    }
                }
            }
        } else {
            System.out.println("Invalid username or password.");
        }
    }
}