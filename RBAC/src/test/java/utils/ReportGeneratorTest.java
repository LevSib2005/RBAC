package utils;

import org.example.*;
import org.example.assignment.PermanentAssignment;
import org.example.entity.*;
import org.example.repository.UserManager;
import org.example.repository.RoleManager;
import org.example.repository.AssignmentManager;
import org.example.utils.ReportGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ReportGeneratorTest {

    private ReportGenerator reportGenerator;
    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;

    @BeforeEach
    void setUp() {
        reportGenerator = new ReportGenerator();
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager(userManager, roleManager);
    }

    private void setupTestData() {
        User user1 = User.create("john_doe", "John Doe", "john@example.com");
        User user2 = User.create("jane_smith", "Jane Smith", "jane@example.com");
        userManager.add(user1);
        userManager.add(user2);

        Permission readUsers = new Permission("READ", "users", "Can read users");
        Permission writeUsers = new Permission("WRITE", "users", "Can write users");
        Permission readReports = new Permission("READ", "reports", "Can read reports");

        Role viewer = new Role("Viewer", "Can view only");
        viewer.addPermission(readUsers);
        viewer.addPermission(readReports);

        Role editor = new Role("Editor", "Can edit");
        editor.addPermission(readUsers);
        editor.addPermission(writeUsers);

        roleManager.add(viewer);
        roleManager.add(editor);

        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Test assignment");
        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Test assignment");

        PermanentAssignment assign1 = new PermanentAssignment(user1, viewer, meta1);
        PermanentAssignment assign2 = new PermanentAssignment(user2, editor, meta2);

        assignmentManager.add(assign1);
        assignmentManager.add(assign2);
    }

    @Test
    void generateUserReportWithEmptyUsers() {
        String report = reportGenerator.generateUserReport(userManager, assignmentManager);
        assertTrue(report.contains("No users found"));
    }

    @Test
    void generateUserReportWithUsers() {
        setupTestData();

        String report = reportGenerator.generateUserReport(userManager, assignmentManager);

        assertTrue(report.contains("USER REPORT"));

        assertTrue(report.contains("john_doe"));
        assertTrue(report.contains("jane_smith"));

        assertTrue(report.contains("Viewer"));
        assertTrue(report.contains("Editor"));

        assertTrue(report.contains("Roles (1):"));
        assertTrue(report.contains("Permissions ("));
        assertTrue(report.contains("Total users: 2"));
    }

    @Test
    void generateUserReportShowsUserFormat() {
        setupTestData();

        String report = reportGenerator.generateUserReport(userManager, assignmentManager);

        assertTrue(report.contains("john_doe (John Doe) <john@example.com>"));
        assertTrue(report.contains("jane_smith (Jane Smith) <jane@example.com>"));
    }

    @Test
    void generateUserReportWithUserWithoutRoles() {
        User user = User.create("testuser", "Test User", "test@example.com");
        userManager.add(user);

        String report = reportGenerator.generateUserReport(userManager, assignmentManager);

        assertTrue(report.contains("testuser"));
        assertTrue(report.contains("Roles (0): none"));
    }

    @Test
    void generateRoleReportWithEmptyRoles() {
        String report = reportGenerator.generateRoleReport(roleManager, assignmentManager);
        assertTrue(report.contains("No roles found"));
    }

    @Test
    void generateRoleReportWithRoles() {
        setupTestData();

        String report = reportGenerator.generateRoleReport(roleManager, assignmentManager);

        assertTrue(report.contains("ROLE REPORT"));

        assertTrue(report.contains("Viewer"));
        assertTrue(report.contains("Editor"));

        assertTrue(report.contains("Description:"));
        assertTrue(report.contains("Permissions:"));
        assertTrue(report.contains("Active users:"));
        assertTrue(report.contains("ID:"));

        assertTrue(report.contains("Total roles: 2"));
    }

    @Test
    void generateRoleReportShowsCorrectActiveUsers() {
        setupTestData();

        String report = reportGenerator.generateRoleReport(roleManager, assignmentManager);

        assertTrue(report.contains("Viewer"));
        assertTrue(report.contains("Active users: 1"));
        assertTrue(report.contains("Editor"));
        assertTrue(report.contains("Active users: 1"));
    }

    @Test
    void generateRoleReportWithRoleWithoutAssignments() {
        Role role = new Role("TestRole", "Test description");
        roleManager.add(role);

        String report = reportGenerator.generateRoleReport(roleManager, assignmentManager);

        assertTrue(report.contains("TestRole"));
        assertTrue(report.contains("Active users: 0"));
    }

    @Test
    void generatePermissionMatrixWithEmptyUsers() {
        String report = reportGenerator.generatePermissionMatrix(userManager, assignmentManager);
        assertTrue(report.contains("No users found"));
    }

    @Test
    void generatePermissionMatrixWithNoPermissions() {
        User user = User.create("testuser", "Test User", "test@example.com");
        userManager.add(user);

        String report = reportGenerator.generatePermissionMatrix(userManager, assignmentManager);

        assertTrue(report.contains("No permissions assigned"));
    }

    @Test
    void generatePermissionMatrixWithPermissions() {
        setupTestData();

        String report = reportGenerator.generatePermissionMatrix(userManager, assignmentManager);

        assertTrue(report.contains("PERMISSION MATRIX"));

        assertTrue(report.contains("john_doe"));
        assertTrue(report.contains("jane_smith"));

        assertTrue(report.contains("users"));
        assertTrue(report.contains("reports"));

        assertTrue(report.contains("READ") || report.contains("READ,WRITE") || report.contains("READ"));
    }

    @Test
    void generatePermissionMatrixShowsCorrectPermissions() {
        setupTestData();

        String report = reportGenerator.generatePermissionMatrix(userManager, assignmentManager);
        String[] lines = report.split("\n");

        String johnLine = null;
        String janeLine = null;

        for (String line : lines) {
            if (line.contains("john_doe")) johnLine = line;
            if (line.contains("jane_smith")) janeLine = line;
        }

        assertNotNull(johnLine);
        assertNotNull(janeLine);

        assertTrue(johnLine.contains("READ") || johnLine.contains("READ,"));

        assertTrue(janeLine.contains("READ,WRITE") || janeLine.contains("WRITE,READ"));
    }

    @Test
    void exportToFileSavesReport(@TempDir Path tempDir) throws IOException {
        setupTestData();

        String report = reportGenerator.generateUserReport(userManager, assignmentManager);
        Path filePath = tempDir.resolve("user_report.txt");

        reportGenerator.exportToFile(report, filePath.toString());

        assertTrue(Files.exists(filePath));

        String fileContent = Files.readString(filePath);
        assertEquals(report, fileContent);
    }

    @Test
    void exportToFileWithInvalidPathHandlesException() {
        String report = "Test report content";

        assertDoesNotThrow(() -> reportGenerator.exportToFile(report, "/invalid/path/report.txt"));
    }

    @Test
    void exportToFileWithEmptyReport(@TempDir Path tempDir) throws IOException {
        String report = "";
        Path filePath = tempDir.resolve("empty.txt");

        reportGenerator.exportToFile(report, filePath.toString());

        assertTrue(Files.exists(filePath));
        assertEquals(0, Files.size(filePath));
    }

    @Test
    void allReportsGeneratedWithSameData() {
        setupTestData();

        String userReport = reportGenerator.generateUserReport(userManager, assignmentManager);
        String roleReport = reportGenerator.generateRoleReport(roleManager, assignmentManager);
        String matrixReport = reportGenerator.generatePermissionMatrix(userManager, assignmentManager);

        assertTrue(userReport.contains("USER REPORT"));
        assertTrue(roleReport.contains("ROLE REPORT"));
        assertTrue(matrixReport.contains("PERMISSION MATRIX"));

        assertTrue(userReport.contains("john_doe"));
        assertTrue(roleReport.contains("Viewer"));
        assertTrue(matrixReport.contains("users"));
    }

    @Test
    void generateUserReportWithMultipleRolesPerUser() {
        User user = User.create("multirole", "Multi Role User", "multi@example.com");
        userManager.add(user);

        Permission readUsers = new Permission("READ", "users", "Read users");
        Permission writeUsers = new Permission("WRITE", "users", "Write users");

        Role role1 = new Role("Role1", "First role");
        role1.addPermission(readUsers);

        Role role2 = new Role("Role2", "Second role");
        role2.addPermission(writeUsers);

        roleManager.add(role1);
        roleManager.add(role2);

        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Test");
        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Test");

        assignmentManager.add(new PermanentAssignment(user, role1, meta1));
        assignmentManager.add(new PermanentAssignment(user, role2, meta2));

        String report = reportGenerator.generateUserReport(userManager, assignmentManager);

        assertTrue(report.contains("Role1"));
        assertTrue(report.contains("Role2"));
        assertTrue(report.contains("Roles (2):"));
    }
}