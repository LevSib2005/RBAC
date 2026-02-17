package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main() {
        User user = User.create("john_doe", "John Doe", "john@mail.com");

        Permission readUsers = new Permission("READ", "users", "Can view users");
        Permission writeUsers = new Permission("WRITE", "users", "Can edit users");

        Role editor = new Role("Editor", "Can edit content");
        editor.addPermission(readUsers);
        editor.addPermission(writeUsers);

        AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Temporary access for project");

        LocalDateTime expiresIn7Days = LocalDateTime.now().plusDays(7);
        String expiresAt = expiresIn7Days.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        TemporaryAssignment tempAssignment = new TemporaryAssignment(
                user, editor, metadata, expiresAt, true
        );

        System.out.println(tempAssignment.summary());

        System.out.println("Is active? " + tempAssignment.isActive());
        System.out.println("Is expired? " + tempAssignment.isExpired());
    }
}