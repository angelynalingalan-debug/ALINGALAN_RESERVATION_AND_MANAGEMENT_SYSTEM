package com.scc.model;

public record Reservation(
        int reservationId,
        int userId,
        int facilityId,
        String reservationDate,
        String purpose,
        String status,
        String username,
        String facilityName
) {}
