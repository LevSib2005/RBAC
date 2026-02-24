package org.example;

import org.example.entity.Permission;
import org.example.entity.Role;
import org.example.repository.RoleManager;
import org.example.filter.RoleFilters;
import org.example.sorter.RoleSorters;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        RoleManager roleManager = new RoleManager();

        System.out.println("ROLE MANAGER TESTS\n");

        Permission readUsers = new Permission("READ", "users", "Can read users");
        Permission writeUsers = new Permission("WRITE", "users", "Can write users");
        Permission deleteUsers = new Permission("DELETE", "users", "Can delete users");
        Permission readReports = new Permission("READ", "reports", "Can read reports");

        System.out.println("Adding roles:");

        Role admin = new Role("Administrator", "Full system access");
        admin.addPermission(readUsers);
        admin.addPermission(writeUsers);
        admin.addPermission(deleteUsers);
        admin.addPermission(readReports);

        Role editor = new Role("Editor", "Can edit content");
        editor.addPermission(readUsers);
        editor.addPermission(writeUsers);
        editor.addPermission(readReports);

        Role viewer = new Role("Viewer", "Can view only");
        viewer.addPermission(readUsers);
        viewer.addPermission(readReports);

        roleManager.add(admin);
        roleManager.add(editor);
        roleManager.add(viewer);

        System.out.println("Total roles: " + roleManager.count());
        System.out.println();

        System.out.println("Find by name 'Editor':");
        roleManager.findByName("Editor")
                .ifPresentOrElse(
                        r -> System.out.println("  Found: " + r.getName() + " - " + r.getDescription()),
                        () -> System.out.println("  Not found")
                );
        System.out.println();

        System.out.println("Exists 'Administrator': " + roleManager.exists("Administrator"));
        System.out.println("Exists 'SuperAdmin': " + roleManager.exists("SuperAdmin"));
        System.out.println();

        System.out.println("Adding DELETE permission to Editor...");
        roleManager.addPermissionToRole("Editor", deleteUsers);

        Role updatedEditor = roleManager.findByName("Editor").get();
        System.out.println("Editor now has " + updatedEditor.getPermissions().size() + " permissions");
        System.out.println();

        System.out.println("Roles with DELETE permission on users:");
        List<Role> rolesWithDelete = roleManager.findRolesWithPermission("DELETE", "users");
        rolesWithDelete.forEach(r -> System.out.println("  " + r.getName()));
        System.out.println();

        System.out.println("Roles with at least 3 permissions:");
        List<Role> rolesWithManyPerms = roleManager.findByFilter(RoleFilters.hasAtLeastNPermissions(3));
        rolesWithManyPerms.forEach(r -> System.out.println("  " + r.getName() + " (" + r.getPermissions().size() + " perms)"));
        System.out.println();

        System.out.println("Roles with READ permission (sorted by permission count):");
        List<Role> readRoles = roleManager.findAll(
                RoleFilters.hasPermission("READ", "users"),
                RoleSorters.byPermissionCount()
        );
        readRoles.forEach(r -> System.out.println("  " + r.getName() + " (" + r.getPermissions().size() + " perms)"));
        System.out.println();

        System.out.println("Removing DELETE permission from Editor...");
        roleManager.removePermissionFromRole("Editor", deleteUsers);

        updatedEditor = roleManager.findByName("Editor").get();
        System.out.println("Editor now has " + updatedEditor.getPermissions().size() + " permissions");
        System.out.println();

        System.out.println("All roles:");
        roleManager.findAll().forEach(r ->
                System.out.println("  " + r.getId() + " - " + r.getName() + " - " + r.getPermissions().size() + " perms")
        );
        System.out.println();

        System.out.println("Error cases:");
        try {
            roleManager.add(admin);
        } catch (IllegalArgumentException e) {
            System.out.println("  OK: " + e.getMessage());
        }

        try {
            roleManager.addPermissionToRole("GhostRole", readUsers); // не существует
        } catch (IllegalArgumentException e) {
            System.out.println("  OK: " + e.getMessage());
        }
    }
}