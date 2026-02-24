package org.example.filter;


import org.example.assignment.RoleAssignment;
import org.example.assignment.TemporaryAssignment;
import org.example.entity.Role;
import org.example.entity.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AssignmentFilters {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private AssignmentFilters() {
    }

    public static AssignmentFilter byUser(User user) {
        return assignment -> assignment.user().equals(user);
    }

    public static AssignmentFilter byUsername(String username) {
        return assignment -> assignment.user().username().equals(username);
    }

    public static AssignmentFilter byRole(Role role) {
        return assignment -> assignment.role().equals(role);
    }

    public static AssignmentFilter byRoleName(String roleName) {
        return assignment -> assignment.role().getName().equals(roleName);
    }

    public static AssignmentFilter activeOnly() {
        return RoleAssignment::isActive;
    }

    public static AssignmentFilter inactiveOnly() {
        return assignment -> !assignment.isActive();
    }

    public static AssignmentFilter byType(String type) {
        return assignment -> assignment.assignmentType().equals(type);
    }

    public static AssignmentFilter assignedBy(String username) {
        return assignment -> assignment.metadata().assignedBy().equals(username);
    }

    public static AssignmentFilter assignedAfter(String date) {
        LocalDateTime filterDate = LocalDateTime.parse(date, FORMATTER);
        return assignment -> {
            LocalDateTime assignedDate = LocalDateTime.parse(assignment.metadata().assignedAt(), FORMATTER);
            return assignedDate.isAfter(filterDate);
        };
    }

    public static AssignmentFilter expiringBefore(String date) {
        LocalDateTime filterDate = LocalDateTime.parse(date, FORMATTER);
        return assignment -> {
            if (assignment instanceof TemporaryAssignment temp) {
                LocalDateTime expireDate = LocalDateTime.parse(temp.getExpiresAt(), FORMATTER);
                return expireDate.isBefore(filterDate);
            }
            return false;
        };
    }
}