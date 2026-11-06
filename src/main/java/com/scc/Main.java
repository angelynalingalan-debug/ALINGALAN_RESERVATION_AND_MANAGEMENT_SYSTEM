    package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        // INITIALIZE DATABASE
        System.out.println("\nInitializing database...");

        DatabaseInitializer.initializeDatabase();

        System.out.println("Database initialized successfully!");

        // CREATE SHARED OBJECTS
        Scanner scanner = new Scanner(System.in);
        UserDao userDao = new UserDao();

        // MAIN SYSTEM TITLE
        System.out.println("\n======================================");
        System.out.println(" COMMUNITY FACILITY RESERVATION SYSTEM");
        System.out.println("======================================");

        try {

            // START AUTHENTICATION
            Authentication.start(scanner, userDao);

        } catch (Exception e) {

            System.out.println("\nSystem error: " + e.getMessage());

        } finally {

            scanner.close();

            System.out.println("\nSystem closed.");
        }
    }
}
