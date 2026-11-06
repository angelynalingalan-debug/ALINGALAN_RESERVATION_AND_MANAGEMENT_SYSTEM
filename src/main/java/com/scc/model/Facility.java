package com.scc.model;

public record Facility(
        int facilityId,
        String facilityName,
        String location,
        String availability
) {}
