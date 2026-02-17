package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TemporaryAssignment extends AbstractRoleAssignment {

    private String expiresAt;
    private boolean autoRenew;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata,
                               String expiresAt, boolean autoRenew) {
        super(user, role, metadata);

        if (expiresAt == null || expiresAt.isEmpty()) {
            throw new IllegalArgumentException("Expires at cannot be null or empty");
        }

        this.expiresAt = expiresAt;
        this.autoRenew = autoRenew;
    }

    @Override
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireDateTime = LocalDateTime.parse(expiresAt, FORMATTER);
        return now.isBefore(expireDateTime);
    }

    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    public boolean isExpired() {
        return !isActive();
    }

    public void extend(String newExpirationDate) {
        if (newExpirationDate == null || newExpirationDate.isEmpty()) {
            throw new IllegalArgumentException("New expiration date cannot be null or empty");
        }
        this.expiresAt = newExpirationDate;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    @Override
    public String summary() {
        String status = isActive() ? "ACTIVE" : "EXPIRED";
        String reason = metadata().reason() != null ? metadata().reason() : "No reason";

        return "[TEMPORARY] " + role().getName() + " assigned to " + user().username() +
                " by " + metadata().assignedBy() + " at " + metadata().assignedAt() +
                "\nReason: " + reason +
                "\nExpires: " + expiresAt +
                "\nAuto-renew: " + (autoRenew ? "Yes" : "No") +
                "\nStatus: " + status;
    }
}