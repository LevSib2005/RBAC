package org.example;

public record Permission(String name, String resource, String description) {
    public Permission {
        if (name == null || resource == null || description == null) {
            throw new IllegalArgumentException("Error: fields cannot be null");
        }

        if (name.isEmpty() || resource.isEmpty() || description.isEmpty()) {
            throw new IllegalArgumentException("Error: fields cannot be empty");
        }

        String normalizedName = name.toUpperCase();
        if (normalizedName.contains(" ")) {
            throw new IllegalArgumentException("Error: name cannot contain spaces");
        }
        name = normalizedName;

        resource = resource.toLowerCase();
    }

    public String format() {
        return name + " on " + resource + ": " + description;
    }

    public boolean matches(String namePattern, String resourcePattern) {
        boolean nameMatches = false;
        boolean resourceMatches = false;

        if (namePattern == null || namePattern.isEmpty()) {
            nameMatches = true;
        } else {
            nameMatches = this.name.contains(namePattern.toUpperCase());
        }

        if (resourcePattern == null || resourcePattern.isEmpty()) {
            resourceMatches = true;
        } else {
            resourceMatches = this.resource.contains(resourcePattern.toLowerCase());
        }

        return nameMatches && resourceMatches;
    }
}