package org.example;

public class Main {
    public static void main(String[] args) {
        Permission readUsers = new Permission("READ", "users", "Can view user list");
        Permission writeUsers = new Permission("WRITE", "users", "Can create and edit users");
        Permission deleteUsers = new Permission("DELETE", "users", "Can delete users");

        Role admin = new Role("Administrator", "Full system access");

        admin.addPermission(readUsers);
        admin.addPermission(writeUsers);
        admin.addPermission(deleteUsers);

        System.out.println(admin.format());

        System.out.println("Has READ on users? " + admin.hasPermission("READ", "users"));
        System.out.println("Has DELETE on reports? " + admin.hasPermission("DELETE", "reports"));

        admin.removePermission(deleteUsers);
        System.out.println("\nAfter removing DELETE permission:");
        System.out.println(admin.format());
    }
}