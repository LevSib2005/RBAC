package commands;

import org.example.commands.CommandParser;
import org.example.commands.CommandRegistry;
import org.example.commands.RBACSystem;
import org.example.entity.Permission;
import org.example.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class CommandRegistryTest {

    private CommandParser parser;
    private RBACSystem system;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
        system = new RBACSystem();
        system.initialize();
        CommandRegistry.registerAll(parser);

        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    private String getOutput() {
        return outputStream.toString();
    }

    private Scanner scannerOf(String... lines) {
        return new Scanner(String.join("\n", lines));
    }

    @Test
    void helpCommandRegistered() {
        parser.executeCommand("help", scannerOf(""), system);
        assertTrue(getOutput().contains("AVAILABLE COMMANDS"));
    }

    @Test
    void statsCommandRegistered() {
        parser.executeCommand("stats", scannerOf(""), system);
        String out = getOutput();
        assertTrue(out.contains("Users:"));
        assertTrue(out.contains("Roles:"));
        assertTrue(out.contains("Assignments:"));
    }

    @Test
    void userListEmpty() {
        system.getUserManager().clear();
        parser.executeCommand("user-list", scannerOf(""), system);
        assertTrue(getOutput().contains("No users found."));
    }

    @Test
    void userListWithUsers() {
        parser.executeCommand("user-list", scannerOf(""), system);
        String out = getOutput();
        assertTrue(out.contains("Username") && out.contains("Full Name") && out.contains("Email"));
        assertTrue(out.contains("admin"));
        assertTrue(out.contains("john_doe"));
    }

    @Test
    void userCreateSuccess() {
        String input = "newuser\nNew User\nnew@mail.com\n";
        parser.executeCommand("user-create", scannerOf(input), system);
        String out = getOutput();
        assertTrue(out.contains("User created: newuser (New User) <new@mail.com>"));
        assertTrue(system.getUserManager().exists("newuser"));
    }

    @Test
    void userCreateDuplicate() {
        String input = "admin\nAnother Admin\nadmin2@mail.com\n";
        parser.executeCommand("user-create", scannerOf(input), system);
        String out = getOutput();
        assertTrue(out.contains("Error: User with username 'admin' already exists"));
    }

    @Test
    void userCreateInvalidData() {
        String input = "a\n\nnotemail\n";
        parser.executeCommand("user-create", scannerOf(input), system);
        String out = getOutput();
        assertTrue(out.contains("Error:"));
    }

    @Test
    void userViewExisting() {
        String input = "admin\n";
        parser.executeCommand("user-view", scannerOf(input), system);
        String out = getOutput();
        assertTrue(out.contains("User: admin (System Administrator) <admin@system.com>"));
        assertTrue(out.contains("Roles ("));
        assertTrue(out.contains("Permissions ("));
    }

    @Test
    void userViewNotFound() {
        String input = "nosuchuser\n";
        parser.executeCommand("user-view", scannerOf(input), system);
        assertTrue(getOutput().contains("User 'nosuchuser' not found."));
    }

    @Test
    void userUpdateExisting() {
        String input = "john_doe\nJohn Updated\njohn.new@mail.com\n";
        parser.executeCommand("user-update", scannerOf(input), system);
        String out = getOutput();
        assertTrue(out.contains("User 'john_doe' updated."));
        var updated = system.getUserManager().findByUsername("john_doe").get();
        assertEquals("John Updated", updated.fullName());
        assertEquals("john.new@mail.com", updated.email());
    }

    @Test
    void userUpdateNotFound() {
        String input = "nosuchuser\n\n\n";
        parser.executeCommand("user-update", scannerOf(input), system);
        assertTrue(getOutput().contains("User 'nosuchuser' not found."));
    }

    @Test
    void userDeleteCancel() {
        String input = "john_doe\nno\n";
        parser.executeCommand("user-delete", scannerOf(input), system);
        String out = getOutput();
        assertTrue(out.contains("Cancelled."));
        assertTrue(system.getUserManager().exists("john_doe"));
    }

    @Test
    void userDeleteConfirm() {
        String input = "john_doe\nyes\n";
        parser.executeCommand("user-delete", scannerOf(input), system);
        String out = getOutput();
        assertTrue(out.contains("deleted"));
        assertFalse(system.getUserManager().exists("john_doe"));
    }

    @Test
    void userSearchByUsername() {
        String input = "1\njohn\n";
        parser.executeCommand("user-search", scannerOf(input), system);
        String out = getOutput();
        assertTrue(out.contains("john_doe"));
        assertFalse(out.contains("bob_wilson"));
        assertFalse(out.contains("jane_smith"));
    }

    @Test
    void userSearchByEmailDomain() {
        String input = "3\nexample.com\n";
        parser.executeCommand("user-search", scannerOf(input), system);
        String out = getOutput();
        assertTrue(out.contains("john_doe"));
        assertTrue(out.contains("jane_smith"));
        assertTrue(out.contains("bob_wilson"));
        assertFalse(out.contains("admin"));
    }

    @Test
    void roleList() {
        parser.executeCommand("role-list", scannerOf(""), system);
        String out = getOutput();
        assertTrue(out.contains("Admin"));
        assertTrue(out.contains("Manager"));
        assertTrue(out.contains("Viewer"));
        assertTrue(out.contains("Permissions"));
    }

    @Test
    void roleCreate() {
        String input = "Tester\nTest role\nyes\nREAD\ndata\nCan read data\nyes\nWRITE\ndata\nCan write data\nno\n";
        parser.executeCommand("role-create", scannerOf(input), system);
        String out = getOutput();
        assertTrue(out.contains("Role 'Tester' created."));
        assertTrue(out.contains("Permission added: READ on data: Can read data"));
        assertTrue(system.getRoleManager().exists("Tester"));
        Role role = system.getRoleManager().findByName("Tester").get();
        assertEquals(2, role.getPermissions().size());
    }

    @Test
    void roleViewExisting() {
        String input = "Admin\n";
        parser.executeCommand("role-view", scannerOf(input), system);
        String out = getOutput();
        assertTrue(out.contains("Admin"));
        assertTrue(out.contains("Full system access"));
        assertTrue(out.contains("Permissions"));
    }

    @Test
    void roleViewNotFound() {
        String input = "Ghost\n";
        parser.executeCommand("role-view", scannerOf(input), system);
        assertTrue(getOutput().contains("Role 'Ghost' not found."));
    }

    @Test
    void roleAddPermission() {
        String input = "Viewer\nEXECUTE\nscripts\nCan run scripts\n";
        parser.executeCommand("role-add-permission", scannerOf(input), system);
        String out = getOutput();
        assertTrue(out.contains("Permission added: EXECUTE on scripts: Can run scripts"));
        Role viewer = system.getRoleManager().findByName("Viewer").get();
        assertTrue(viewer.hasPermission("EXECUTE", "scripts"));
    }

    @Test
    void roleRemovePermission() {
        Role viewer = system.getRoleManager().findByName("Viewer").get();
        Permission perm = new Permission("TEST", "x", "desc");
        viewer.addPermission(perm);
        String input = "Viewer\n1\n";
        parser.executeCommand("role-remove-permission", scannerOf(input), system);
        String out = getOutput();
        assertTrue(out.contains("Permission removed:"));
    }

    @Test
    void assignRolePermanent() {
        List<Role> roles = system.getRoleManager().findAll();
        int adminIndex = -1;
        for (int i = 0; i < roles.size(); i++) {
            if (roles.get(i).getName().equals("Admin")) {
                adminIndex = i + 1;
                break;
            }
        }
        assertNotEquals(-1, adminIndex, "Role 'Admin' not found in role list");

        String input = "john_doe\n" + adminIndex + "\n1\n\n";
        parser.executeCommand("assign-role", scannerOf(input), system);
        String out = getOutput();
        assertTrue(out.contains("Permanent assignment created."));

        var user = system.getUserManager().findByUsername("john_doe").get();
        var assignments = system.getAssignmentManager().findByUser(user);
        assertTrue(assignments.stream().anyMatch(a -> a.role().getName().equals("Admin") && a.isActive()));
    }

    @Test
    void assignRoleTemporary() {
        List<Role> roles = system.getRoleManager().findAll();
        int managerIndex = -1;
        for (int i = 0; i < roles.size(); i++) {
            if (roles.get(i).getName().equals("Manager")) {
                managerIndex = i + 1;
                break;
            }
        }
        assertNotEquals(-1, managerIndex, "Role 'Manager' not found");

        String input = "john_doe\n" + managerIndex + "\n2\nTemporary access\n2025-12-31 23:59\ny\n";
        parser.executeCommand("assign-role", scannerOf(input), system);
        String out = getOutput();
        assertTrue(out.contains("Temporary assignment created."));
    }

    @Test
    void revokeRole() {
        String assignInput = "bob_wilson\n1\n1\n\n";
        parser.executeCommand("assign-role", scannerOf(assignInput), system);
        outputStream.reset();

        String revokeInput = "bob_wilson\n1\n";
        parser.executeCommand("revoke-role", scannerOf(revokeInput), system);
        String out = getOutput();
        assertTrue(out.contains("Assignment revoked."));
    }

    @Test
    void assignmentList() {
        parser.executeCommand("assignment-list", scannerOf(""), system);
        String out = getOutput();
        assertTrue(out.contains("Username") && out.contains("Role") && out.contains("Status"));
        assertTrue(out.contains("admin"));
    }

    @Test
    void permissionsUser() {
        String input = "admin\n";
        parser.executeCommand("permissions-user", scannerOf(input), system);
        String out = getOutput();
        assertTrue(out.contains("Permissions for 'admin':"));
        assertTrue(out.contains("Resource: users"));
        assertTrue(out.contains("- READ:"));
    }

    @Test
    void permissionsCheckYes() {
        String input = "admin\nREAD\nusers\n";
        parser.executeCommand("permissions-check", scannerOf(input), system);
        String out = getOutput();
        assertTrue(out.contains("YES: 'admin' has READ on users"));
        assertTrue(out.contains("Granted by role(s):"));
    }

    @Test
    void permissionsCheckNo() {
        String input = "john_doe\nDELETE\nusers\n";
        parser.executeCommand("permissions-check", scannerOf(input), system);
        String out = getOutput();
        assertTrue(out.contains("NO: 'john_doe' does NOT have DELETE on users"));
    }

    @Test
    void clearCommand() {
        parser.executeCommand("clear", scannerOf(""), system);
        assertNotNull(getOutput());
    }

    @Test
    void exitCommandCancelled() {
        String input = "no\n";
        parser.executeCommand("exit", scannerOf(input), system);
        assertTrue(getOutput().contains("Exit cancelled."));
    }

    @Test
    void saveNotImplemented() {
        parser.executeCommand("save", scannerOf(""), system);
        assertTrue(getOutput().contains("Save feature is not implemented."));
    }

    @Test
    void loadNotImplemented() {
        parser.executeCommand("load", scannerOf(""), system);
        assertTrue(getOutput().contains("Load feature is not implemented."));
    }
}