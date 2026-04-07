package syncrepository;

import org.example.entity.User;
import org.example.repository.UserManager;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {

    private UserManager userManager;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
    }

    @Test
    void testConcurrentAddSameUsername() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        String username = "john";
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    userManager.add(User.create(username, "John Doe", "john@example.com"));
                } catch (IllegalArgumentException ignored) {}
            });
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS));
        assertEquals(1, userManager.count());
        assertTrue(userManager.findByUsername(username).isPresent());
    }

    @Test
    void testConcurrentAddDifferentUsers() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        int userCount = 20;
        for (int i = 0; i < userCount; i++) {
            final int idx = i;
            executor.submit(() -> userManager.add(User.create("user" + idx, "Name", "email" + idx + "@test.com")));
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS));
        assertEquals(userCount, userManager.count());
    }

    @Test
    void testConcurrentRemove() throws Exception {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            User u = User.create("user" + i, "Name", "email" + i + "@test.com");
            userManager.add(u);
            users.add(u);
        }
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (User u : users) {
            executor.submit(() -> userManager.remove(u));
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS));
        assertEquals(0, userManager.count());
    }

    @Test
    void testConcurrentReadWrite() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(6);
        AtomicInteger added = new AtomicInteger();
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            executor.submit(() -> {
                String name = "writer" + idx;
                if (!userManager.exists(name)) {
                    userManager.add(User.create(name, "Full", name + "@test.com"));
                    added.incrementAndGet();
                }
            });
        }
        for (int i = 0; i < 3; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 10; j++) {
                    userManager.exists("random" + j);
                    userManager.findByUsername("nonexistent");
                    userManager.findAll();
                    userManager.count();
                }
            });
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS));
        assertEquals(added.get(), userManager.count());
    }

    @Test
    void testConcurrentUpdate() throws Exception {
        User user = User.create("alice", "Alice Brown", "alice@test.com");
        userManager.add(user);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                userManager.update("alice", "Alice Updated", "alice" + idx + "@test.com");
                return null;
            }));
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
        Optional<User> updated = userManager.findByUsername("alice");
        assertTrue(updated.isPresent());
        assertNotEquals("Alice Brown", updated.get().fullName());
    }

    @Test
    void testConcurrentAddAndUpdate() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    userManager.add(User.create("bob", "Bob", "bob@test.com"));
                } catch (IllegalArgumentException ignored) {}
            });
        }
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    userManager.update("bob", "Robert", "robert@test.com");
                } catch (IllegalArgumentException | IllegalStateException ignored) {}
            });
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
        assertEquals(1, userManager.count());
        Optional<User> bob = userManager.findByUsername("bob");
        assertTrue(bob.isPresent());
    }

    @Test
    void testFindByEmailConcurrent() throws Exception {
        for (int i = 0; i < 20; i++) {
            userManager.add(User.create("user" + i, "Name", "user" + i + "@test.com"));
        }
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<Optional<User>>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> userManager.findByEmail("user" + idx + "@test.com")));
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS));
        for (int i = 0; i < 10; i++) {
            Optional<User> found = futures.get(i).get();
            assertTrue(found.isPresent());
            assertEquals("user" + i + "@test.com", found.get().email());
        }
    }
}