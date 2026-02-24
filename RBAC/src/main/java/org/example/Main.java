package org.example;

import org.example.entity.Permission;
import org.example.entity.Role;
import org.example.filter.RoleFilter;
import org.example.filter.RoleFilters;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Permission readUsers = new Permission("READ", "users", "Can read users");
        Permission writeUsers = new Permission("WRITE", "users", "Can write users");
        Permission deleteUsers = new Permission("DELETE", "users", "Can delete users");
        Permission readReports = new Permission("READ", "reports", "Can read reports");
        Permission writeReports = new Permission("WRITE", "reports", "Can write reports");

        Role admin = new Role("Administrator", "Full access");
        admin.addPermission(readUsers);
        admin.addPermission(writeUsers);
        admin.addPermission(deleteUsers);
        admin.addPermission(readReports);
        admin.addPermission(writeReports);

        Role editor = new Role("Editor", "Can edit content");
        editor.addPermission(readUsers);
        editor.addPermission(writeUsers);
        editor.addPermission(readReports);

        Role viewer = new Role("Viewer", "Can view only");
        viewer.addPermission(readUsers);
        viewer.addPermission(readReports);

        Role guest = new Role("Guest", "Limited access");
        guest.addPermission(readUsers);

        List<Role> roles = List.of(admin, editor, viewer, guest);

        System.out.println("Test 1: byName 'Administrator'");
        filterAndPrint(roles, RoleFilters.byName("Administrator"));

        System.out.println("Test 2: byNameContains 'edit'");
        filterAndPrint(roles, RoleFilters.byNameContains("edit"));

        System.out.println("Test 3: hasPermission DELETE on users");
        filterAndPrint(roles, RoleFilters.hasPermission("DELETE", "users"));

        System.out.println("Test 4: hasAtLeastNPermissions(3)");
        filterAndPrint(roles, RoleFilters.hasAtLeastNPermissions(3));

        System.out.println("Test 5: hasPermission READ on reports AND at least 2 permissions");
        RoleFilter filterAnd = RoleFilters.hasPermission("READ", "reports")
                .and(RoleFilters.hasAtLeastNPermissions(2));
        filterAndPrint(roles, filterAnd);

        System.out.println("Test 6: name contains 'view' OR has DELETE permission");
        RoleFilter filterOr = RoleFilters.byNameContains("view")
                .or(RoleFilters.hasPermission("DELETE", "users"));
        filterAndPrint(roles, filterOr);

        System.out.println("Test 7: (name contains 'Admin' OR has WRITE on reports) AND at least 3 permissions");
        RoleFilter complex = RoleFilters.byNameContains("Admin")
                .or(RoleFilters.hasPermission("WRITE", "reports"))
                .and(RoleFilters.hasAtLeastNPermissions(3));
        filterAndPrint(roles, complex);
    }

    private static void filterAndPrint(List<Role> roles, RoleFilter filter) {
        roles.stream()
                .filter(filter::test)
                .forEach(role -> System.out.println("  " + role.getName() + " (" + role.getPermissions().size() + " permissions)"));

        long count = roles.stream().filter(filter::test).count();
        System.out.println("  Found: " + count + " roles\n");
    }
}