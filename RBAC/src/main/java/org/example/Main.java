package org.example;

public class Main {
    public static void main(String[] args) {

        System.out.println("Test 1: Valid user");
        try {
            User user = User.create("john_doe", "John Doe", "john@mail.com");
            System.out.println("OK: " + user);
            System.out.println("Format: " + user.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println();

        System.out.println("Test 2: Empty username");
        try {
            User user = User.create("", "John Doe", "john@mail.com");
            System.out.println("OK: " + user);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println();

        System.out.println("Test 3: Username with @");
        try {
            User user = User.create("john@doe", "John Doe", "john@mail.com");
            System.out.println("OK: " + user);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println();

        System.out.println("Test 4: Email without @");
        try {
            User user = User.create("jane_doe", "Jane Doe", "janemail.com");
            System.out.println("OK: " + user);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println();

        System.out.println("Test 5: Email without dot after @");
        try {
            User user = User.create("bob_smith", "Bob Smith", "bob@mailcom");
            System.out.println("OK: " + user);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println();
    }
}