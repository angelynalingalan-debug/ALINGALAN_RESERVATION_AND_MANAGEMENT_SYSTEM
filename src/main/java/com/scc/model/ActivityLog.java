package com.scc.model;

public record ActivityLog(
        int logId,
        Integer userId,
        String actionType,
        String description,
        String timestamp
) {}
