package org.example;

import org.example.entity.User;
import org.example.filter.UserFilter;
import org.example.filter.UserFilters;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<User> users = List.of(
                User.create("john_doe", "John Doe", "john@company.com"),
                User.create("jane_smith", "Jane Smith", "jane@gmail.com"),
                User.create("bob_johnson", "Bob Johnson", "bob@company.com"),
                User.create("alice_wonder", "Alice Wonder", "alice@company.com"),
                User.create("charlie_brown", "Charlie Brown", "charlie@gmail.com")
        );

        System.out.println("--- Test 1: byUsername 'john_doe' ---");
        filterAndPrint(users, UserFilters.byUsername("john_doe"));

        System.out.println("--- Test 2: byUsernameContains 'john' ---");
        filterAndPrint(users, UserFilters.byUsernameContains("john"));

        System.out.println("--- Test 3: byEmailDomain '@company.com' ---");
        filterAndPrint(users, UserFilters.byEmailDomain("@company.com"));

        System.out.println("--- Test 4: byFullNameContains 'Smith' ---");
        filterAndPrint(users, UserFilters.byFullNameContains("Smith"));

        System.out.println("--- Test 5: AND combination ---");
        UserFilter filterAnd = UserFilters.byUsernameContains("john")
                .and(UserFilters.byEmailDomain("@company.com"));
        filterAndPrint(users, filterAnd);

        System.out.println("--- Test 6: OR combination ---");
        UserFilter filterOr = UserFilters.byUsernameContains("john")
                .or(UserFilters.byEmailDomain("@gmail.com"));
        filterAndPrint(users, filterOr);

        System.out.println("--- Test 7: Complex combination ---");
        UserFilter complex = UserFilters.byFullNameContains("John")
                .or(UserFilters.byFullNameContains("Charlie"))
                .and(UserFilters.byEmailDomain("@company.com"));
        filterAndPrint(users, complex);
    }

    private static void filterAndPrint(List<User> users, UserFilter filter) {
        users.stream()
                .filter(filter::test)
                .forEach(user -> System.out.println("  " + user.format()));

        long count = users.stream().filter(filter::test).count();
        System.out.println("  Found: " + count + " users\n");
    }
}