package org.example;

import java.util.regex.Pattern;

public record User(String username, String fullName, String email) {

    public static User create(String username, String fullName, String email) {
        if (username == null || fullName == null || email == null) {
            throw new IllegalArgumentException("Error: fields cannot be null");
        }

        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
            throw new IllegalArgumentException("Error: fields cannot be empty");
        }

        if (username.length() < 3 || username.length() > 20) {
            throw new IllegalArgumentException("Error: username must be 3-20 characters long");
        }

        Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9_]+$");
        if (!usernamePattern.matcher(username).matches()) {
            throw new IllegalArgumentException("Error: username can only contain letters, numbers, and underscore");
        }

        int atIndex = email.indexOf('@');
        if (atIndex < 0) {
            throw new IllegalArgumentException("Error: email must contain @");
        }

        int dotIndex = email.indexOf('.', atIndex);
        if (dotIndex < 0) {
            throw new IllegalArgumentException("Error: email must have a dot after @");
        }

        return new User(username, fullName, email);
    }

    public String format() {
        return username + " (" + fullName + ") <" + email + ">";
    }
}