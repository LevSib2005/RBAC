package org.example;

import org.example.entity.User;
import org.example.repository.UserManager;
import org.example.filter.UserFilters;
import org.example.sorter.UserSorters;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        UserManager userManager = new UserManager();

        System.out.println("USER MANAGER TESTS\n");

        System.out.println("Adding users:");

        User user1 = User.create("john_doe", "John Doe", "john@mail.com");
        User user2 = User.create("jane_smith", "Jane Smith", "jane@mail.com");
        User user3 = User.create("bob_johnson", "Bob Johnson", "bob@work.com");
        User user4 = User.create("alice_w", "Alice Wonder", "alice@work.com");

        userManager.add(user1);
        userManager.add(user2);
        userManager.add(user3);
        userManager.add(user4);

        System.out.println("Total users: " + userManager.count());
        System.out.println();

        System.out.println("Find by username 'john_doe':");
        userManager.findByUsername("john_doe")
                .ifPresentOrElse(
                        u -> System.out.println("  Found: " + u.format()),
                        () -> System.out.println("  Not found")
                );
        System.out.println();

        System.out.println("Find by email 'jane@mail.com':");
        userManager.findByEmail("jane@mail.com")
                .ifPresentOrElse(
                        u -> System.out.println("  Found: " + u.format()),
                        () -> System.out.println("  Not found")
                );
        System.out.println();

        System.out.println("Check exists 'bob_johnson': " + userManager.exists("bob_johnson"));
        System.out.println("Check exists 'unknown': " + userManager.exists("unknown"));
        System.out.println();

        System.out.println("Users with '@work.com' domain:");
        List<User> workUsers = userManager.findByFilter(UserFilters.byEmailDomain("@work.com"));
        workUsers.forEach(u -> System.out.println("  " + u.format()));
        System.out.println();

        System.out.println("Users with 'john' in username (sorted by full name):");
        List<User> johnUsers = userManager.findAll(
                UserFilters.byUsernameContains("john"),
                UserSorters.byFullName()
        );
        johnUsers.forEach(u -> System.out.println("  " + u.format()));
        System.out.println();

        System.out.println("Updating john_doe...");
        userManager.update("john_doe", "John Updated", "john.new@mail.com");
        userManager.findByUsername("john_doe")
                .ifPresent(u -> System.out.println("  Updated: " + u.format()));
        System.out.println();

        System.out.println("Removing alice_w...");
        userManager.remove(user4);
        System.out.println("Total users after removal: " + userManager.count());
        System.out.println();

        System.out.println("Error cases:");
        try {
            userManager.add(user1); // дубликат
        } catch (IllegalArgumentException e) {
            System.out.println("  OK: " + e.getMessage());
        }

        try {
            userManager.update("unknown", "Name", "email@mail.com"); // не существует
        } catch (IllegalArgumentException e) {
            System.out.println("  OK: " + e.getMessage());
        }
    }
}