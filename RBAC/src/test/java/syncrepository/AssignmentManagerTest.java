package syncrepository;

import org.example.assignment.PermanentAssignment;
import org.example.assignment.RoleAssignment;
import org.example.entity.AssignmentMetadata;
import org.example.entity.Permission;
import org.example.entity.Role;
import org.example.entity.User;
import org.example.repository.AssignmentManager;
import org.example.repository.RoleManager;
import org.example.repository.UserManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentManagerTest {

    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;
    private User alice;
    private Role admin;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager(userManager, roleManager);

        alice = User.create("alice", "Alice Brown", "alice@example.com");
        admin = new Role("ADMIN", "Administrator");
        userManager.add(alice);
        roleManager.add(admin);
    }

    private AssignmentMetadata createMetadata() {
        return AssignmentMetadata.now("system", "test");
    }

    @Test
    void testConcurrentAddSameAssignment() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(5);

        PermanentAssignment assignment = new PermanentAssignment(alice, admin, createMetadata());
        String id = assignment.assignmentId();

        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    assignmentManager.add(assignment);
                } catch (IllegalArgumentException ignored) {
                }
            });
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));

        assertEquals(1, assignmentManager.count());
        assertTrue(assignmentManager.findById(id).isPresent());
    }

    @Test
    void testConcurrentAddDifferentAssignments() throws Exception {
        int count = 20;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < count; i++) {
            final String roleName = "ROLE_" + i;
            final String userName = "user" + i;
            Role r = new Role(roleName, "desc");
            roleManager.add(r);
            User u = User.create(userName, "Name", userName + "@test.com");
            userManager.add(u);
            final Role finalRole = r;
            final User finalUser = u;
            executor.submit(() -> {
                assignmentManager.add(new PermanentAssignment(finalUser, finalRole, createMetadata()));
            });
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
        assertEquals(count, assignmentManager.count());
    }

    @Test
    void testConcurrentRemove() throws Exception {
        List<RoleAssignment> assignments = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            final String roleName = "ROLE_" + i;
            final String userName = "user" + i;
            Role r = new Role(roleName, "desc");
            roleManager.add(r);
            User u = User.create(userName, "Name", userName + "@test.com");
            userManager.add(u);
            PermanentAssignment a = new PermanentAssignment(u, r, createMetadata());
            assignmentManager.add(a);
            assignments.add(a);
        }
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (RoleAssignment a : assignments) {
            executor.submit(() -> assignmentManager.remove(a));
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
        assertEquals(0, assignmentManager.count());
    }

    @Test
    void testConcurrentRevoke() throws Exception {
        PermanentAssignment assignment = new PermanentAssignment(alice, admin, createMetadata());
        assignmentManager.add(assignment);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                try {
                    assignmentManager.revokeAssignment(assignment.assignmentId());
                } catch (IllegalArgumentException ignored) {
                }
            });
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
        RoleAssignment result = assignmentManager.findById(assignment.assignmentId()).orElseThrow();
        assertFalse(result.isActive());
    }

    @Test
    void testConcurrentUserHasRole() throws Exception {
        for (int i = 0; i < 10; i++) {
            Role r = new Role("ROLE_" + i, "desc");
            roleManager.add(r);
            assignmentManager.add(new PermanentAssignment(alice, r, createMetadata()));
        }
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                Role role = roleManager.findByName("ROLE_" + idx).orElseThrow();
                return assignmentManager.userHasRole(alice, role);
            }));
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS));
        for (Future<Boolean> f : futures) {
            assertTrue(f.get());
        }
    }

    @Test
    void testConcurrentGetUserPermissions() throws Exception {
        Permission readPerm = new Permission("READ", "doc", "read docs");
        admin.addPermission(readPerm);
        assignmentManager.add(new PermanentAssignment(alice, admin, createMetadata()));
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<Set<Permission>>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            futures.add(executor.submit(() -> assignmentManager.getUserPermissions(alice)));
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS));
        for (Future<Set<Permission>> f : futures) {
            Set<Permission> perms = f.get();
            assertEquals(1, perms.size());
            assertTrue(perms.contains(readPerm));
        }
    }

    @Test
    void testConcurrentAddAndCheckActive() throws Exception {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    assignmentManager.add(new PermanentAssignment(alice, admin, createMetadata()));
                } catch (IllegalArgumentException ignored) {
                }
                return null;
            });
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));

        long activeCount = assignmentManager.getActiveAssignments().stream()
                .filter(a -> a.user().equals(alice) && a.role().equals(admin))
                .count();
        assertEquals(1, activeCount);
        assertEquals(1, assignmentManager.count());
    }

    @Test
    void testConcurrentUserHasPermission() throws Exception {
        Permission perm = new Permission("WRITE", "report", "write reports");
        admin.addPermission(perm);
        assignmentManager.add(new PermanentAssignment(alice, admin, createMetadata()));
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            futures.add(executor.submit(() ->
                    assignmentManager.userHasPermission(alice, "WRITE", "report")
            ));
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS));
        for (Future<Boolean> f : futures) {
            assertTrue(f.get());
        }
    }
}