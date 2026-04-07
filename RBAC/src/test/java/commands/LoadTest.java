package commands;

import org.example.assignment.PermanentAssignment;
import org.example.commands.RBACSystem;
import org.example.entity.*;
import org.junit.jupiter.api.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RBAC Load Tests")
class LoadTest {

    private RBACSystem system;

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
        system.initialize();
    }

    @Test
    @DisplayName("Concurrent user creation – no duplicates, no crashes")
    void testConcurrentUserCreation() throws InterruptedException {
        int initialCount = system.getUserManager().count();
        int threads = 5;
        int opsPerThread = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);

        for (int t = 0; t < threads; t++) {
            final int tid = t;
            pool.submit(() -> {
                for (int i = 0; i < opsPerThread; i++) {
                    String name = "user_" + tid + "_" + i;
                    try {
                        system.getUserManager().add(User.create(name, "Test", name + "@test.com"));
                        success.incrementAndGet();
                    } catch (IllegalArgumentException e) {
                        fail.incrementAndGet();
                    }
                }
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        pool.shutdown();

        assertEquals(threads * opsPerThread, success.get());
        assertEquals(0, fail.get());
        assertEquals(initialCount + threads * opsPerThread, system.getUserManager().count());
    }

    @Test
    @DisplayName("Concurrent role creation – no duplicates")
    void testConcurrentRoleCreation() throws InterruptedException {
        int threads = 5;
        int opsPerThread = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger(0);

        for (int t = 0; t < threads; t++) {
            final int tid = t;
            pool.submit(() -> {
                for (int i = 0; i < opsPerThread; i++) {
                    String name = "role_" + tid + "_" + i;
                    try {
                        system.getRoleManager().add(new Role(name, "desc"));
                        success.incrementAndGet();
                    } catch (IllegalArgumentException ignored) {}
                }
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        pool.shutdown();

        assertEquals(threads * opsPerThread, success.get());
        assertEquals(3 + threads * opsPerThread, system.getRoleManager().count());
    }

    @Test
    @DisplayName("Concurrent assignments – no corruption")
    void testConcurrentAssignments() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            system.getUserManager().add(User.create("assignUser" + i, "Name", "email" + i + "@test.com"));
            system.getRoleManager().add(new Role("assignRole" + i, "desc"));
        }

        int threads = 5;
        int opsPerThread = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger(0);

        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                for (int i = 0; i < opsPerThread; i++) {
                    try {
                        int idx = ThreadLocalRandom.current().nextInt(10);
                        User u = system.getUserManager().findByUsername("assignUser" + idx).get();
                        Role r = system.getRoleManager().findByName("assignRole" + idx).get();
                        AssignmentMetadata meta = AssignmentMetadata.now("load", "test");
                        system.getAssignmentManager().add(new PermanentAssignment(u, r, meta));
                        success.incrementAndGet();
                    } catch (IllegalArgumentException ignored) {}
                }
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        pool.shutdown();

        assertTrue(success.get() > 0);
        assertTrue(system.getAssignmentManager().count() > 0);
    }

    @Test
    @DisplayName("Concurrent reads – no exceptions")
    void testConcurrentReads() throws InterruptedException {
        int threads = 10;
        int opsPerThread = 100;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger errors = new AtomicInteger(0);

        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                for (int i = 0; i < opsPerThread; i++) {
                    try {
                        system.getUserManager().findAll();
                        system.getRoleManager().findAll();
                        system.getAssignmentManager().getActiveAssignments();
                        system.getAssignmentManager().userHasRole(
                                system.getUserManager().findByUsername("admin").get(),
                                system.getRoleManager().findByName("Admin").get()
                        );
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    }
                }
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        pool.shutdown();

        assertEquals(0, errors.get());
    }
}