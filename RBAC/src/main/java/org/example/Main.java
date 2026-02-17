package org.example;

public class Main {
    public static void main() {
        Permission p1 = new Permission("READ", "users", "Can read user data");
        System.out.println("Valid: " + p1.format());

        Permission p2 = new Permission("delete", "USERS", "Can delete");
        System.out.println("Normalized: " + p2.name() + " on " + p2.resource());

        System.out.println("Matches READ on users? " + p1.matches("READ", "users"));
        System.out.println("Matches WRITE on users? " + p1.matches("WRITE", "users"));

    }
}