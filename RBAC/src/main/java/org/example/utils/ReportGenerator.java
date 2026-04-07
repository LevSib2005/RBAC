package org.example.utils;

import org.example.assignment.RoleAssignment;
import org.example.entity.*;
import org.example.repository.UserManager;
import org.example.repository.RoleManager;
import org.example.repository.AssignmentManager;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {

    public String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder report = new StringBuilder();
        report.append("USER REPORT\n\n");

        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            report.append("No users found\n");
            return report.toString();
        }

        for (User user : users) {
            report.append("User: ").append(user.format()).append("\n");

            List<RoleAssignment> active = assignmentManager.findByUser(user).stream()
                    .filter(RoleAssignment::isActive)
                    .collect(Collectors.toList());

            report.append("  Roles (").append(active.size()).append("):");
            if (active.isEmpty()) {
                report.append(" none\n");
            } else {
                report.append("\n");
                for (RoleAssignment ra : active) {
                    report.append("    - ").append(ra.role().getName())
                            .append(" [").append(ra.assignmentType()).append("]\n");
                }
            }

            Set<Permission> perms = assignmentManager.getUserPermissions(user);
            report.append("  Permissions (").append(perms.size()).append("):");
            if (perms.isEmpty()) {
                report.append(" none\n");
            } else {
                report.append("\n");
                for (Permission p : perms) {
                    report.append("    - ").append(p.format()).append("\n");
                }
            }
            report.append("\n");
        }

        report.append("Total users: ").append(users.size()).append("\n");
        return report.toString();
    }

    public String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        StringBuilder report = new StringBuilder();
        report.append("ROLE REPORT\n\n");

        List<Role> roles = roleManager.findAll();
        if (roles.isEmpty()) {
            report.append("No roles found\n");
            return report.toString();
        }

        for (Role role : roles) {
            long activeUsers = assignmentManager.findByRole(role).stream()
                    .filter(RoleAssignment::isActive)
                    .count();

            report.append("Role: ").append(role.getName()).append("\n");
            report.append("  Description: ").append(role.getDescription()).append("\n");
            report.append("  Permissions: ").append(role.getPermissions().size()).append("\n");
            report.append("  Active users: ").append(activeUsers).append("\n");
            report.append("  ID: ").append(role.getId()).append("\n\n");
        }

        report.append("Total roles: ").append(roles.size()).append("\n");
        return report.toString();
    }

    public String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder report = new StringBuilder();
        report.append("PERMISSION MATRIX\n\n");

        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            report.append("No users found\n");
            return report.toString();
        }

        Set<String> allResources = new TreeSet<>();
        for (User user : users) {
            Set<Permission> perms = assignmentManager.getUserPermissions(user);
            for (Permission p : perms) {
                allResources.add(p.resource());
            }
        }

        if (allResources.isEmpty()) {
            report.append("No permissions assigned\n");
            return report.toString();
        }

        List<String> resourceList = new ArrayList<>(allResources);

        report.append(String.format("%-15s", "User"));
        for (String resource : resourceList) {
            report.append(String.format(" | %-15s", resource));
        }
        report.append("\n");
        report.append("-".repeat(15 + resourceList.size() * 18)).append("\n");

        for (User user : users) {
            Set<Permission> perms = assignmentManager.getUserPermissions(user);
            report.append(String.format("%-15s", user.username()));

            for (String resource : resourceList) {
                String permNames = perms.stream()
                        .filter(p -> p.resource().equals(resource))
                        .map(Permission::name)
                        .sorted()
                        .collect(Collectors.joining(","));

                if (permNames.isEmpty()) {
                    report.append(String.format(" | %-15s", "-"));
                } else {
                    report.append(String.format(" | %-15s", permNames));
                }
            }
            report.append("\n");
        }

        return report.toString();
    }

    public void exportToFile(String report, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.print(report);
            System.out.println("Report saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving report: " + e.getMessage());
        }
    }

    public String generateUserReportParallel(UserManager userManager, AssignmentManager assignmentManager) {
        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            return "USER REPORT\n\nNo users found\n";
        }

        String userReports = users.parallelStream()
                .map(user -> buildUserReportSection(user, assignmentManager))
                .collect(Collectors.joining());

        StringBuilder report = new StringBuilder();
        report.append("USER REPORT\n\n");
        report.append(userReports);
        report.append("Total users: ").append(users.size()).append("\n");
        return report.toString();
    }

    private String buildUserReportSection(User user, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("User: ").append(user.format()).append("\n");

        List<RoleAssignment> active = assignmentManager.findByUser(user).stream()
                .filter(RoleAssignment::isActive)
                .collect(Collectors.toList());

        sb.append("  Roles (").append(active.size()).append("):");
        if (active.isEmpty()) {
            sb.append(" none\n");
        } else {
            sb.append("\n");
            for (RoleAssignment ra : active) {
                sb.append("    - ").append(ra.role().getName())
                        .append(" [").append(ra.assignmentType()).append("]\n");
            }
        }

        Set<Permission> perms = assignmentManager.getUserPermissions(user);
        sb.append("  Permissions (").append(perms.size()).append("):");
        if (perms.isEmpty()) {
            sb.append(" none\n");
        } else {
            sb.append("\n");
            for (Permission p : perms) {
                sb.append("    - ").append(p.format()).append("\n");
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    public String generatePermissionMatrixParallel(UserManager userManager, AssignmentManager assignmentManager) {
        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            return "PERMISSION MATRIX\n\nNo users found\n";
        }

        Set<String> allResources = users.parallelStream()
                .flatMap(user -> assignmentManager.getUserPermissions(user).stream())
                .map(Permission::resource)
                .collect(Collectors.toCollection(TreeSet::new));

        if (allResources.isEmpty()) {
            return "PERMISSION MATRIX\n\nNo permissions assigned\n";
        }

        List<String> resourceList = new ArrayList<>(allResources);

        StringBuilder header = new StringBuilder();
        header.append("PERMISSION MATRIX\n\n");
        header.append(String.format("%-15s", "User"));
        for (String resource : resourceList) {
            header.append(String.format(" | %-15s", resource));
        }
        header.append("\n");
        header.append("-".repeat(15 + resourceList.size() * 18)).append("\n");

        String rows = users.parallelStream()
                .map(user -> buildPermissionMatrixRow(user, resourceList, assignmentManager))
                .collect(Collectors.joining());

        return header.toString() + rows;
    }

    private String buildPermissionMatrixRow(User user, List<String> resourceList, AssignmentManager assignmentManager) {
        Set<Permission> perms = assignmentManager.getUserPermissions(user);
        StringBuilder row = new StringBuilder();
        row.append(String.format("%-15s", user.username()));

        for (String resource : resourceList) {
            String permNames = perms.stream()
                    .filter(p -> p.resource().equals(resource))
                    .map(Permission::name)
                    .sorted()
                    .collect(Collectors.joining(","));
            if (permNames.isEmpty()) {
                row.append(String.format(" | %-15s", "-"));
            } else {
                row.append(String.format(" | %-15s", permNames));
            }
        }
        row.append("\n");
        return row.toString();
    }
}