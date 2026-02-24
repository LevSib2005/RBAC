package org.example.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AssignmentMetadata {
        if (assignedBy == null || assignedBy.isEmpty()) {
            throw new IllegalArgumentException("assignedBy cannot be null or empty");
        }
        if (assignedAt == null || assignedAt.isEmpty()) {
            throw new IllegalArgumentException("assignedAt cannot be null or empty");
        }
    }

    public static AssignmentMetadata now(String assignedBy, String reason) {
        String currentTime = LocalDateTime.now().format(FORMATTER);
        return new AssignmentMetadata(assignedBy, currentTime, reason);
    }

    public static AssignmentMetadata now(String assignedBy) {
        return now(assignedBy, null);
    }

    public String format() {
        if (reason == null || reason.isEmpty()) {
            return "Assigned by: " + assignedBy + " at " + assignedAt;
        } else {
            return "Assigned by: " + assignedBy + " at " + assignedAt + " (Reason: " + reason + ")";
        }
    }
}