package syncrepository;

import org.example.entity.Permission;
import org.example.entity.Role;
import org.example.repository.RoleManager;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RoleManagerTest {

    private RoleManager roleManager;
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        roleManager = new RoleManager();
        executor = Executors.newFixedThreadPool(10);
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    void testConcurrentAddSameRoleName() throws Exception {
        int threadCount = 5;
        String roleName = "ADMIN";
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Void>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                startLatch.await();
                Role role = new Role(roleName, "desc");
                try {
                    roleManager.add(role);
                } catch (IllegalArgumentException ignored) {
                }
                return null;
            }));
        }

        startLatch.countDown();
        for (Future<Void> f : futures) {
            f.get(5, TimeUnit.SECONDS);
        }

        assertEquals(1, roleManager.count());
        assertTrue(roleManager.findByName(roleName).isPresent());
    }

    @Test
    void testConcurrentAddDifferentRoles() throws Exception {
        int roleCount = 20;
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Void>> futures = new ArrayList<>();

        for (int i = 0; i < roleCount; i++) {
            final String name = "ROLE_" + i;
            futures.add(executor.submit(() -> {
                startLatch.await();
                roleManager.add(new Role(name, "desc"));
                return null;
            }));
        }

        startLatch.countDown();
        for (Future<Void> f : futures) {
            f.get(5, TimeUnit.SECONDS);
        }

        assertEquals(roleCount, roleManager.count());
        for (int i = 0; i < roleCount; i++) {
            assertTrue(roleManager.findByName("ROLE_" + i).isPresent());
        }
    }

    @Test
    void testConcurrentRemove() throws Exception {
        List<Role> roles = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Role r = new Role("ROLE_" + i, "desc");
            roleManager.add(r);
            roles.add(r);
        }

        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Void>> futures = new ArrayList<>();

        for (Role r : roles) {
            futures.add(executor.submit(() -> {
                startLatch.await();
                roleManager.remove(r);
                return null;
            }));
        }

        startLatch.countDown();
        for (Future<Void> f : futures) {
            f.get(5, TimeUnit.SECONDS);
        }

        assertEquals(0, roleManager.count());
    }

    @Test
    void testConcurrentReadWrite() throws Exception {
        int writerThreads = 5;
        int readerThreads = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successAdds = new AtomicInteger(0);
        List<Future<Void>> futures = new ArrayList<>();

        for (int i = 0; i < writerThreads; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                startLatch.await();
                String name = "WRITER_" + idx;
                if (!roleManager.exists(name)) {
                    roleManager.add(new Role(name, "desc"));
                    successAdds.incrementAndGet();
                }
                return null;
            }));
        }

        for (int i = 0; i < readerThreads; i++) {
            futures.add(executor.submit(() -> {
                startLatch.await();
                for (int j = 0; j < 20; j++) {
                    roleManager.exists("RANDOM_" + j);
                    roleManager.findByName("NON_EXISTENT");
                    roleManager.findAll();
                    roleManager.count();
                }
                return null;
            }));
        }

        startLatch.countDown();
        for (Future<Void> f : futures) {
            f.get(5, TimeUnit.SECONDS);
        }

        assertEquals(successAdds.get(), roleManager.count());
    }

    @Test
    void testConcurrentAddPermissionsToSameRole() throws Exception {
        Role role = new Role("TEST_ROLE", "desc");
        roleManager.add(role);

        int threadCount = 20;
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Void>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                startLatch.await();
                Permission perm = new Permission("ACTION_" + idx, "resource", "desc");
                roleManager.addPermissionToRole("TEST_ROLE", perm);
                return null;
            }));
        }

        startLatch.countDown();
        for (Future<Void> f : futures) {
            f.get(5, TimeUnit.SECONDS);
        }

        Role updated = roleManager.findByName("TEST_ROLE").orElseThrow();
        assertEquals(threadCount, updated.getPermissions().size());
    }

    @Test
    void testConcurrentRemovePermissionsFromSameRole() throws Exception {
        Role role = new Role("TEST_ROLE", "desc");
        List<Permission> perms = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Permission p = new Permission("ACTION_" + i, "resource", "desc");
            role.addPermission(p);
            perms.add(p);
        }
        roleManager.add(role);

        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Void>> futures = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                startLatch.await();
                roleManager.removePermissionFromRole("TEST_ROLE", perms.get(idx));
                return null;
            }));
        }

        startLatch.countDown();
        for (Future<Void> f : futures) {
            f.get(5, TimeUnit.SECONDS);
        }

        Role updated = roleManager.findByName("TEST_ROLE").orElseThrow();
        assertEquals(0, updated.getPermissions().size());
    }

    @Test
    void testConcurrentAddAndRemovePermissions() throws Exception {
        Role role = new Role("TEST_ROLE", "desc");
        roleManager.add(role);

        List<Permission> toRemove = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Permission p = new Permission("REMOVE_" + i, "resource", "desc");
            role.addPermission(p);
            toRemove.add(p);
        }

        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Void>> futures = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                startLatch.await();
                roleManager.removePermissionFromRole("TEST_ROLE", toRemove.get(idx));
                return null;
            }));
        }

        for (int i = 0; i < 20; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                startLatch.await();
                Permission newPerm = new Permission("NEW_" + idx, "resource", "desc");
                roleManager.addPermissionToRole("TEST_ROLE", newPerm);
                return null;
            }));
        }

        startLatch.countDown();
        for (Future<Void> f : futures) {
            f.get(5, TimeUnit.SECONDS);
        }

        Role updated = roleManager.findByName("TEST_ROLE").orElseThrow();
        assertEquals(20, updated.getPermissions().size());
        for (Permission p : toRemove) {
            assertFalse(updated.hasPermission(p));
        }
    }

    @Test
    void testConcurrentFindRolesWithPermission() throws Exception {
        Permission targetPerm = new Permission("READ", "doc", "read");
        for (int i = 0; i < 20; i++) {
            Role r = new Role("ROLE_" + i, "desc");
            if (i % 2 == 0) {
                r.addPermission(targetPerm);
            }
            roleManager.add(r);
        }

        int threadCount = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                startLatch.await();
                return roleManager.findRolesWithPermission("READ", "doc").size();
            }));
        }

        startLatch.countDown();
        for (Future<Integer> f : futures) {
            assertEquals(10, f.get(5, TimeUnit.SECONDS).intValue());
        }
    }

    @Test
    void testConcurrentAddDuplicateThrowsOnlyOnce() throws Exception {
        int threads = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                startLatch.await();
                try {
                    roleManager.add(new Role("DUPLICATE", "desc"));
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }));
        }

        startLatch.countDown();
        long successCount = 0;
        for (Future<Boolean> f : futures) {
            if (f.get(5, TimeUnit.SECONDS)) {
                successCount++;
            }
        }

        assertEquals(1, successCount);
        assertEquals(1, roleManager.count());
    }

    @Test
    void testConcurrentReadWhileModifyingPermissions() throws Exception {
        Role role = new Role("STRESS_ROLE", "desc");
        roleManager.add(role);
        for (int i = 0; i < 5; i++) {
            role.addPermission(new Permission("BASE_" + i, "resource", "desc"));
        }

        int modifierThreads = 3;
        int readerThreads = 3;
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Void>> futures = new ArrayList<>();

        for (int i = 0; i < modifierThreads; i++) {
            futures.add(executor.submit(() -> {
                startLatch.await();
                Random rand = new Random();
                for (int op = 0; op < 30; op++) {
                    Permission p = new Permission("DYN_" + op, "resource", "desc");
                    if (rand.nextBoolean()) {
                        roleManager.addPermissionToRole("STRESS_ROLE", p);
                    } else {
                        roleManager.removePermissionFromRole("STRESS_ROLE", p);
                    }
                }
                return null;
            }));
        }

        for (int i = 0; i < readerThreads; i++) {
            futures.add(executor.submit(() -> {
                startLatch.await();
                for (int q = 0; q < 30; q++) {
                    Role r = roleManager.findByName("STRESS_ROLE").orElseThrow();
                    assertNotNull(r);
                }
                return null;
            }));
        }

        startLatch.countDown();
        for (Future<Void> f : futures) {
            f.get(5, TimeUnit.SECONDS);
        }

        Role finalRole = roleManager.findByName("STRESS_ROLE").orElseThrow();
        assertNotNull(finalRole);
    }
}