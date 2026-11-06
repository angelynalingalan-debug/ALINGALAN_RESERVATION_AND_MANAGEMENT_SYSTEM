
package com.scc.ui;

import com.scc.dao.FacilityDao;
import com.scc.dao.ReservationDao;
import com.scc.dao.UserDao;
import com.scc.model.Facility;
import com.scc.model.Reservation;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class StaffMenu {

    public static void showMenu(
            Scanner scanner,
            User staff,
            UserDao userDao) {

        FacilityDao facilityDao = new FacilityDao();
        ReservationDao reservationDao = new ReservationDao();

        boolean active = true;

        while (active) {

            System.out.println("\n================================");
            System.out.println("         STAFF DASHBOARD");
            System.out.println("================================");
            System.out.println("Welcome, " + staff.username());
            System.out.println("--------------------------------");
            System.out.println("1. View All Facilities");
            System.out.println("2. View All Reservations");
            System.out.println("3. Manage Reservations");
            System.out.println("4. Logout");
            System.out.println("--------------------------------");

            int choice = Authentication.readInt(
                    scanner, "Enter your choice: ");

            switch (choice) {

                // VIEW ALL FACILITIES
                case 1 -> {

                    System.out.println("\n=== FACILITY LIST ===");

                    List<Facility> facilities =
                            facilityDao.getAllFacilities();

                    if (facilities.isEmpty()) {
                        System.out.println("No facilities available.");
                    } else {
                        for (Facility facility : facilities) {
                            System.out.println(facility);
                        }
                    }
                }

                // VIEW ALL RESERVATIONS
                case 2 -> {

                    System.out.println("\n=== ALL RESERVATIONS ===");

                    List<Reservation> reservations =
                            reservationDao.getAllReservations();

                    if (reservations.isEmpty()) {
                        System.out.println("No reservations found.");
                    } else {
                        for (Reservation reservation : reservations) {
                            System.out.println(reservation);
                        }
                    }
                }

                // MANAGE RESERVATIONS
                case 3 -> {

                    System.out.println("\n=== MANAGE RESERVATIONS ===");

                    int reservationId = Authentication.readInt(
                            scanner, "Enter Reservation ID: ");

                    System.out.print(
                            "Enter New Status (APPROVED/REJECTED/CANCELLED): ");

                    String status = scanner.nextLine()
                            .trim()
                            .toUpperCase();

                    if (!status.equals("APPROVED")
                            && !status.equals("REJECTED")
                            && !status.equals("CANCELLED")) {

                        System.out.println("Invalid status!");
                        System.out.println(
                                "Please enter APPROVED, REJECTED, or CANCELLED.");

                        break;
                    }

                    boolean updated =
                            reservationDao.updateReservationStatus(
                                    staff.userId(),
                                    reservationId,
                                    status
                            );

                    if (updated) {
                        System.out.println(
                                "Reservation updated successfully.");

                        System.out.println(
                                "Audit log recorded successfully.");
                    } else {
                        System.out.println(
                                "Failed to update reservation.");

                        System.out.println(
                                "Please check the Reservation ID and try again.");
                    }
                }

                // LOGOUT
                case 4 -> {

                    userDao.logout(staff);

                    System.out.println(
                            "You have successfully logged out.");

                    active = false;
                }

                // INVALID CHOICE
                default -> {
                    System.out.println(
                            "Invalid choice. Please select 1-4.");
                }
            }
        }
    }
}
