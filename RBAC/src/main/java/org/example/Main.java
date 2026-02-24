package org.example;

import org.example.assignment.PermanentAssignment;
import org.example.assignment.RoleAssignment;
import org.example.assignment.TemporaryAssignment;
import org.example.entity.AssignmentMetadata;
import org.example.entity.Permission;
import org.example.entity.Role;
import org.example.entity.User;
import org.example.filter.AssignmentFilter;
import org.example.filter.AssignmentFilters;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        User user1 = User.create("john_doe", "John Doe", "john@mail.com");
        User user2 = User.create("jane_smith", "Jane Smith", "jane@mail.com");
        User admin = User.create("admin", "Admin User", "admin@system.com");

        Permission readUsers = new Permission("READ", "users", "Can read users");
        Permission writeUsers = new Permission("WRITE", "users", "Can write users");

        Role viewer = new Role("Viewer", "Can view only");
        viewer.addPermission(readUsers);

        Role editor = new Role("Editor", "Can edit");
        editor.addPermission(readUsers);
        editor.addPermission(writeUsers);

        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");
        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Project access");
        AssignmentMetadata meta3 = AssignmentMetadata.now("manager", "Temporary access");

        PermanentAssignment perm1 = new PermanentAssignment(user1, viewer, meta1);
        PermanentAssignment perm2 = new PermanentAssignment(user2, editor, meta2);

        LocalDateTime expiresSoon = LocalDateTime.now().plusDays(2);
        String expiresAt = expiresSoon.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        TemporaryAssignment temp1 = new TemporaryAssignment(user1, editor, meta3, expiresAt, false);

        perm2.revoke();

        List<RoleAssignment> assignments = List.of(perm1, perm2, temp1);

        System.out.println("ASSIGNMENT FILTER TESTS\n");

        System.out.println("Test 1: byUser (john_doe)");
        filterAndPrint(assignments, AssignmentFilters.byUser(user1));

        System.out.println("Test 2: byRoleName 'Editor'");
        filterAndPrint(assignments, AssignmentFilters.byRoleName("Editor"));

        System.out.println("Test 3: activeOnly");
        filterAndPrint(assignments, AssignmentFilters.activeOnly());

        System.out.println("Test 4: byType 'TEMPORARY'");
        filterAndPrint(assignments, AssignmentFilters.byType("TEMPORARY"));

        System.out.println("Test 5: assignedBy 'admin'");
        filterAndPrint(assignments, AssignmentFilters.assignedBy("admin"));

        System.out.println("Test 6: byUser AND activeOnly");
        AssignmentFilter filterAnd = AssignmentFilters.byUser(user1)
                .and(AssignmentFilters.activeOnly());
        filterAndPrint(assignments, filterAnd);

        System.out.println("Test 7: byType 'PERMANENT' OR inactiveOnly");
        AssignmentFilter filterOr = AssignmentFilters.byType("PERMANENT")
                .or(AssignmentFilters.inactiveOnly());
        filterAndPrint(assignments, filterOr);
    }

    private static void filterAndPrint(List<RoleAssignment> assignments, AssignmentFilter filter) {
        assignments.stream()
                .filter(filter::test)
                .forEach(a -> {
                    String type = a.assignmentType();
                    String status = a.isActive() ? "ACTIVE" : "INACTIVE";
                    System.out.println("  [" + type + "] " + a.user().username() +
                            " -> " + a.role().getName() +
                            " (" + status + ")");
                });

        long count = assignments.stream().filter(filter::test).count();
        System.out.println("  Found: " + count + " assignments\n");
    }
}