package org.example;

import org.example.assignment.PermanentAssignment;
import org.example.assignment.RoleAssignment;
import org.example.assignment.TemporaryAssignment;
import org.example.entity.AssignmentMetadata;
import org.example.entity.Permission;
import org.example.entity.Role;
import org.example.entity.User;
import org.example.repository.UserManager;
import org.example.repository.RoleManager;
import org.example.repository.AssignmentManager;
import org.example.filter.AssignmentFilters;
import org.example.sorter.AssignmentSorters;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        System.out.println("ASSIGNMENT MANAGER TESTS\n");

        UserManager userManager = new UserManager();
        RoleManager roleManager = new RoleManager();
        AssignmentManager assignmentManager = new AssignmentManager(userManager, roleManager);

        User user1 = User.create("john_doe", "John Doe", "john@mail.com");
        User user2 = User.create("jane_smith", "Jane Smith", "jane@mail.com");

        userManager.add(user1);
        userManager.add(user2);

        Permission readUsers = new Permission("READ", "users", "Can read users");
        Permission writeUsers = new Permission("WRITE", "users", "Can write users");
        Permission deleteUsers = new Permission("DELETE", "users", "Can delete users");

        Role viewer = new Role("Viewer", "Can view only");
        viewer.addPermission(readUsers);

        Role editor = new Role("Editor", "Can edit");
        editor.addPermission(readUsers);
        editor.addPermission(writeUsers);

        Role admin = new Role("Admin", "Full access");
        admin.addPermission(readUsers);
        admin.addPermission(writeUsers);
        admin.addPermission(deleteUsers);

        roleManager.add(viewer);
        roleManager.add(editor);
        roleManager.add(admin);

        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");
        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Project access");
        AssignmentMetadata meta3 = AssignmentMetadata.now("manager", "Temporary access");

        PermanentAssignment perm1 = new PermanentAssignment(user1, viewer, meta1);
        PermanentAssignment perm2 = new PermanentAssignment(user2, admin, meta2);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String expiresAt = LocalDateTime.now().plusDays(5).format(formatter);
        TemporaryAssignment temp1 = new TemporaryAssignment(user1, editor, meta3, expiresAt, true);

        System.out.println("Adding assignments:");
        assignmentManager.add(perm1);
        assignmentManager.add(perm2);
        assignmentManager.add(temp1);

        System.out.println("Total assignments: " + assignmentManager.count());
        System.out.println();

        System.out.println("Assignments for john_doe:");
        List<RoleAssignment> userAssignments = assignmentManager.findByUser(user1);
        userAssignments.forEach(a ->
                System.out.println("  " + a.assignmentType() + " - " + a.role().getName())
        );
        System.out.println();

        System.out.println("Active assignments:");
        List<RoleAssignment> active = assignmentManager.getActiveAssignments();
        active.forEach(a ->
                System.out.println("  " + a.user().username() + " -> " + a.role().getName())
        );
        System.out.println();

        System.out.println("Does john_doe have Editor role? " +
                assignmentManager.userHasRole(user1, editor));
        System.out.println("Does john_doe have Admin role? " +
                assignmentManager.userHasRole(user1, admin));
        System.out.println();

        System.out.println("Does john_doe have WRITE permission on users? " +
                assignmentManager.userHasPermission(user1, "WRITE", "users"));
        System.out.println("Does john_doe have DELETE permission on users? " +
                assignmentManager.userHasPermission(user1, "DELETE", "users"));
        System.out.println();

        System.out.println("All permissions for john_doe:");
        Set<Permission> perms = assignmentManager.getUserPermissions(user1);
        perms.forEach(p -> System.out.println("  " + p.format()));
        System.out.println();

        System.out.println("Temporary assignments:");
        List<RoleAssignment> tempAssignments = assignmentManager.findByFilter(
                AssignmentFilters.byType("TEMPORARY")
        );
        tempAssignments.forEach(a ->
                System.out.println("  " + a.user().username() + " -> " + a.role().getName())
        );
        System.out.println();

        System.out.println("Assignments sorted by username:");
        List<RoleAssignment> sorted = assignmentManager.findAll(
                null,
                AssignmentSorters.byUsername()
        );
        sorted.forEach(a ->
                System.out.println("  " + a.user().username() + " -> " + a.role().getName())
        );
        System.out.println();

        System.out.println("Revoking permanent assignment for jane_smith...");
        assignmentManager.revokeAssignment(perm2.assignmentId());

        System.out.println("Active assignments after revoke:");
        active = assignmentManager.getActiveAssignments();
        active.forEach(a ->
                System.out.println("  " + a.user().username() + " -> " + a.role().getName())
        );
        System.out.println();

        System.out.println("Error cases:");

        try {
            assignmentManager.add(perm1);
        } catch (IllegalArgumentException e) {
            System.out.println("  OK: " + e.getMessage());
        }

        try {
            User fakeUser = User.create("fake", "Fake User", "fake@mail.com");
            AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
            PermanentAssignment fake = new PermanentAssignment(fakeUser, viewer, meta);
            assignmentManager.add(fake);
        } catch (IllegalArgumentException e) {
            System.out.println("  OK: " + e.getMessage());
        }

        try {
            assignmentManager.revokeAssignment(temp1.assignmentId());
        } catch (IllegalArgumentException e) {
            System.out.println("  OK: " + e.getMessage());
        }
    }
}