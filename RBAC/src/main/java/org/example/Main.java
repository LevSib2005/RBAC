package org.example;

import org.example.assignment.PermanentAssignment;
import org.example.assignment.RoleAssignment;
import org.example.assignment.TemporaryAssignment;
import org.example.entity.AssignmentMetadata;
import org.example.entity.Permission;
import org.example.entity.Role;
import org.example.entity.User;
import org.example.sorter.UserSorters;
import org.example.sorter.RoleSorters;
import org.example.sorter.AssignmentSorters;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class Main {
    public static void main(String[] args) {
        testUserSorters();
        testRoleSorters();
        testAssignmentSorters();
    }

    private static void testUserSorters() {
        System.out.println("USER SORTING TESTS");

        List<User> users = new ArrayList<>();
        users.add(User.create("john_doe", "John Doe", "john@mail.com"));
        users.add(User.create("alice_w", "Alice Wonder", "alice@mail.com"));
        users.add(User.create("bob_smith", "Bob Smith", "bob@mail.com"));
        users.add(User.create("charlie", "Charlie Brown", "charlie@mail.com"));

        System.out.println("\nOriginal order:");
        users.forEach(u -> System.out.println("  " + u.format()));

        Collections.sort(users, UserSorters.byUsername());
        System.out.println("\nSorted by username:");
        users.forEach(u -> System.out.println("  " + u.format()));

        Collections.sort(users, UserSorters.byFullName());
        System.out.println("\nSorted by full name:");
        users.forEach(u -> System.out.println("  " + u.format()));

        Collections.sort(users, UserSorters.byEmail());
        System.out.println("\nSorted by email:");
        users.forEach(u -> System.out.println("  " + u.format()));

        System.out.println();
    }

    private static void testRoleSorters() {
        System.out.println("ROLE SORTING TESTS");

        Permission p1 = new Permission("READ", "users", "read");
        Permission p2 = new Permission("WRITE", "users", "write");
        Permission p3 = new Permission("DELETE", "users", "delete");

        Role admin = new Role("Administrator", "Admin role");
        admin.addPermission(p1);
        admin.addPermission(p2);
        admin.addPermission(p3);

        Role viewer = new Role("Viewer", "View only");
        viewer.addPermission(p1);

        Role editor = new Role("Editor", "Edit content");
        editor.addPermission(p1);
        editor.addPermission(p2);

        List<Role> roles = new ArrayList<>();
        roles.add(admin);
        roles.add(viewer);
        roles.add(editor);

        System.out.println("\nOriginal order:");
        roles.forEach(r -> System.out.println("  " + r.getName() + " (" + r.getPermissions().size() + " perms)"));

        Collections.sort(roles, RoleSorters.byName());
        System.out.println("\nSorted by name:");
        roles.forEach(r -> System.out.println("  " + r.getName() + " (" + r.getPermissions().size() + " perms)"));

        Collections.sort(roles, RoleSorters.byPermissionCount());
        System.out.println("\nSorted by permission count:");
        roles.forEach(r -> System.out.println("  " + r.getName() + " (" + r.getPermissions().size() + " perms)"));

        System.out.println();
    }

    private static void testAssignmentSorters() {
        System.out.println("ASSIGNMENT SORTING TESTS");

        User user1 = User.create("john_doe", "John Doe", "john@mail.com");
        User user2 = User.create("alice_w", "Alice Wonder", "alice@mail.com");
        User user3 = User.create("bob_smith", "Bob Smith", "bob@mail.com");

        Permission p1 = new Permission("READ", "users", "read");
        Permission p2 = new Permission("WRITE", "users", "write");

        Role viewer = new Role("Viewer", "View only");
        viewer.addPermission(p1);

        Role editor = new Role("Editor", "Edit content");
        editor.addPermission(p1);
        editor.addPermission(p2);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        AssignmentMetadata meta1 = new AssignmentMetadata("admin",
                LocalDateTime.now().minusDays(5).format(formatter), "First");
        AssignmentMetadata meta2 = new AssignmentMetadata("admin",
                LocalDateTime.now().minusDays(1).format(formatter), "Second");
        AssignmentMetadata meta3 = new AssignmentMetadata("manager",
                LocalDateTime.now().format(formatter), "Third");

        PermanentAssignment perm1 = new PermanentAssignment(user1, viewer, meta1);
        PermanentAssignment perm2 = new PermanentAssignment(user2, editor, meta2);

        String expiresAt = LocalDateTime.now().plusDays(10).format(formatter);
        TemporaryAssignment temp1 = new TemporaryAssignment(user3, editor, meta3, expiresAt, true);

        List<RoleAssignment> assignments = new ArrayList<>();
        assignments.add(perm1);
        assignments.add(perm2);
        assignments.add(temp1);

        System.out.println("\nOriginal order:");
        assignments.forEach(a -> System.out.println("  " + a.user().username() + " -> " +
                a.role().getName() + " at " + a.metadata().assignedAt()));

        Collections.sort(assignments, AssignmentSorters.byUsername());
        System.out.println("\nSorted by username:");
        assignments.forEach(a -> System.out.println("  " + a.user().username() + " -> " +
                a.role().getName() + " at " + a.metadata().assignedAt()));

        Collections.sort(assignments, AssignmentSorters.byRoleName());
        System.out.println("\nSorted by role name:");
        assignments.forEach(a -> System.out.println("  " + a.user().username() + " -> " +
                a.role().getName() + " at " + a.metadata().assignedAt()));

        Collections.sort(assignments, AssignmentSorters.byAssignmentDate());
        System.out.println("\nSorted by assignment date:");
        assignments.forEach(a -> System.out.println("  " + a.user().username() + " -> " +
                a.role().getName() + " at " + a.metadata().assignedAt()));

        System.out.println();
    }
}