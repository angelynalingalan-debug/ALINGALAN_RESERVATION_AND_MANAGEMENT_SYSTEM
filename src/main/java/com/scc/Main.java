package com.scc;

import com.scc.dao.DatabaseConnection;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // 1. Automatically initialize SQLite database and tables on startup
        DatabaseConnection.initializeDatabase();

        // 2. Instantiate shared dependencies
        Scanner scanner = new Scanner(System.in);
        UserDao userDao = new UserDao();

        System.out.println("===================================================");
        System.out.println(" Community Facility Reservation & Management System ");
        System.out.println("===================================================\n");

        // 3. Delegate execution flow strictly to Authentication
        Authentication.start(scanner, userDao);

        scanner.close();
    }
}