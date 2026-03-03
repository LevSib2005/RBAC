package org.example.commands;

import org.example.assignment.PermanentAssignment;
import org.example.entity.*;
import org.example.repository.UserManager;
import org.example.repository.RoleManager;
import org.example.repository.AssignmentManager;

public class RBACSystem {

    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;
    private String currentUser;

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager(userManager, roleManager);
        this.currentUser = "system";
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public AssignmentManager getAssignmentManager() {
        return assignmentManager;
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void initialize() {
        Permission readUsers = new Permission("READ", "users", "Can view users");
        Permission writeUsers = new Permission("WRITE", "users", "Can create/edit users");
        Permission deleteUsers = new Permission("DELETE", "users", "Can delete users");

        Permission readReports = new Permission("READ", "reports", "Can view reports");
        Permission writeReports = new Permission("WRITE", "reports", "Can create/edit reports");
        Permission deleteReports = new Permission("DELETE", "reports", "Can delete reports");

        Permission readSettings = new Permission("READ", "settings", "Can view settings");
        Permission writeSettings = new Permission("WRITE", "settings", "Can change settings");

        Role adminRole = new Role("Admin", "Full system access");
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        adminRole.addPermission(deleteUsers);
        adminRole.addPermission(readReports);
        adminRole.addPermission(writeReports);
        adminRole.addPermission(deleteReports);
        adminRole.addPermission(readSettings);
        adminRole.addPermission(writeSettings);

        Role managerRole = new Role("Manager", "Can manage users and view reports");
        managerRole.addPermission(readUsers);
        managerRole.addPermission(writeUsers);
        managerRole.addPermission(readReports);
        managerRole.addPermission(writeReports);
        managerRole.addPermission(readSettings);

        Role viewerRole = new Role("Viewer", "Can view only");
        viewerRole.addPermission(readUsers);
        viewerRole.addPermission(readReports);
        viewerRole.addPermission(readSettings);

        roleManager.add(adminRole);
        roleManager.add(managerRole);
        roleManager.add(viewerRole);

        User admin = User.create("admin", "System Administrator", "admin@system.com");
        userManager.add(admin);
        setCurrentUser("admin");

        AssignmentMetadata metadata = AssignmentMetadata.now("system", "Initial system setup");
        PermanentAssignment adminAssignment = new PermanentAssignment(admin, adminRole, metadata);
        assignmentManager.add(adminAssignment);

        User user1 = User.create("john_doe", "John Doe", "john@example.com");
        User user2 = User.create("jane_smith", "Jane Smith", "jane@example.com");
        User user3 = User.create("bob_wilson", "Bob Wilson", "bob@example.com");

        userManager.add(user1);
        userManager.add(user2);
        userManager.add(user3);

        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Regular employee");
        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Team lead");
        AssignmentMetadata meta3 = AssignmentMetadata.now("admin", "Contractor");

        PermanentAssignment assign1 = new PermanentAssignment(user1, viewerRole, meta1);
        PermanentAssignment assign2 = new PermanentAssignment(user2, managerRole, meta2);
        PermanentAssignment assign3 = new PermanentAssignment(user3, viewerRole, meta3);

        assignmentManager.add(assign1);
        assignmentManager.add(assign2);
        assignmentManager.add(assign3);
    }

    public String generateStatistics() {
        StringBuilder stats = new StringBuilder();

        stats.append("Current user: ").append(currentUser).append("\n\n");

        stats.append("Users: ").append(userManager.count()).append("\n");
        stats.append("Roles: ").append(roleManager.count()).append("\n");
        stats.append("Assignments: ").append(assignmentManager.count()).append("\n\n");

        stats.append("Active assignments: ").append(assignmentManager.getActiveAssignments().size()).append("\n");
        stats.append("Expired assignments: ").append(assignmentManager.getExpiredAssignments().size()).append("\n");

        return stats.toString();
    }
}