package com.scc.ui;

import com.scc.dao.FacilityDao;
import com.scc.dao.ReservationDao;
import com.scc.dao.UserDao;

import com.scc.model.User;
import com.scc.model.Facility;
import com.scc.model.Reservation;

import java.util.List;
import java.util.Scanner;

public class ResidentMenu {

    public static void showMenu(
            Scanner scanner,
            User resident,
            UserDao userDao) {

        FacilityDao facilityDao = new FacilityDao();
        ReservationDao reservationDao = new ReservationDao();

        boolean active = true;

        while (active) {

            System.out.println("\n==========================================");
            System.out.println("             RESIDENT DASHBOARD");
            System.out.println("==========================================");
            System.out.println("Welcome, " + resident.username());
            System.out.println("------------------------------------------");
            System.out.println("1. View Available Facilities");
            System.out.println("2. Create Reservation");
            System.out.println("3. View My Reservations");
            System.out.println("4. Logout");
            System.out.println("==========================================");

            int choice = Authentication.readInt(
                    scanner, "Enter your choice: ");

            switch (choice) {

                // 1. VIEW AVAILABLE FACILITIES
                case 1 -> {

                    List<Facility> facilities =
                            facilityDao.getAllFacilities();

                    displayFacilities(facilities);
                }

                // 2. CREATE RESERVATION
                case 2 -> {

                    System.out.println("\n================================");
                    System.out.println("        CREATE RESERVATION");
                    System.out.println("================================");

                    List<Facility> facilities =
                            facilityDao.getAllFacilities();

                    if (facilities == null || facilities.isEmpty()) {
                        System.out.println("No facilities available.");
                        break;
                    }

                    displayFacilities(facilities);

                    int facilityId = Authentication.readInt(
                            scanner, "\nEnter Facility ID: ");

                    System.out.print(
                            "Reservation Date (YYYY-MM-DD): ");

                    String date = scanner.nextLine().trim();

                    System.out.print("Purpose of Reservation: ");

                    String purpose = scanner.nextLine().trim();

                    if (date.isEmpty() || purpose.isEmpty()) {
                        System.out.println(
                                "Date and purpose cannot be empty.");
                        break;
                    }

                    boolean created =
                            reservationDao.createReservation(
                                    resident.userId(),
                                    facilityId,
                                    date,
                                    purpose
                            );

                    if (created) {

                        System.out.println(
                                "\nReservation created successfully!");

                        System.out.println(
                                "Your reservation is PENDING approval.");

                        System.out.println(
                                "Please wait for the administrator's decision.");

                    } else {

                        System.out.println(
                                "\nReservation failed.");

                        System.out.println(
                                "Please check the Facility ID and try again.");
                    }
                }

                // 3. VIEW MY RESERVATIONS
                case 3 -> {

                    System.out.println("\n=== MY RESERVATIONS ===");

                    List<Reservation> reservations =
                            reservationDao.getReservationsByUserId(
                                    resident.userId());

                    displayMyReservations(reservations);
                }

                // 4. LOGOUT
                case 4 -> {

                    userDao.logout(resident);

                    System.out.println(
                            "\nYou have successfully logged out.");

                    active = false;
                }

                default -> System.out.println(
                        "Invalid choice. Please select 1-4.");
            }
        }
    }

    // DISPLAY AVAILABLE FACILITIES
    private static void displayFacilities(
            List<Facility> facilities) {

        System.out.println(
                "\n================ AVAILABLE FACILITIES ================");

        if (facilities == null || facilities.isEmpty()) {

            System.out.println("No facilities available.");
            return;
        }

        String line =
                "+--------+----------------------+----------------------+------------+";

        System.out.println(line);

        System.out.printf(
                "| %-6s | %-20s | %-20s | %-10s |%n",
                "FAC ID",
                "FACILITY",
                "LOCATION",
                "STATUS"
        );

        System.out.println(line);

        for (Facility f : facilities) {

            System.out.printf(
                    "| %-6d | %-20s | %-20s | %-10s |%n",
                    f.facilityId(),
                    fit(f.facilityName(), 20),
                    fit(f.location(), 20),
                    fit(f.availability(), 10)
            );
        }

        System.out.println(line);
    }

    // DISPLAY RESIDENT'S RESERVATIONS
    private static void displayMyReservations(
            List<Reservation> reservations) {

        System.out.println(
                "\n========================== MY RESERVATIONS ==========================");

        if (reservations == null || reservations.isEmpty()) {

            System.out.println(
                    "You don't have any reservations yet.");

            System.out.println(
                    "=====================================================================");

            return;
        }

        String line =
                "+--------+---------+--------+------------+----------------------+--------------------------+------------+";

        System.out.println(line);

        System.out.printf(
                "| %-6s | %-7s | %-6s | %-10s | %-20s | %-24s | %-10s |%n",
                "RES ID",
                "USER ID",
                "FAC ID",
                "DATE",
                "FACILITY",
                "PURPOSE",
                "STATUS"
        );

        System.out.println(line);

        for (Reservation r : reservations) {

            System.out.printf(
                    "| %-6d | %-7d | %-6d | %-10s | %-20s | %-24s | %-10s |%n",
                    r.reservationId(),
                    r.userId(),
                    r.facilityId(),
                    fit(r.reservationDate(), 10),
                    fit(r.facilityName(), 20),
                    fit(r.purpose(), 24),
                    fit(r.status(), 10)
            );
        }

        System.out.println(line);

        System.out.println(
                "Total Reservations: " + reservations.size());

        System.out.println(
                "=====================================================================");
    }

    // LIMIT TEXT TO COLUMN WIDTH
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
