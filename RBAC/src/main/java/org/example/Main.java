package org.example;

public class Main {
    public static void main(String[] args) {
        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "New project");
        System.out.println(meta1.format());

        AssignmentMetadata meta2 = AssignmentMetadata.now("john_doe");
        System.out.println(meta2.format());

        AssignmentMetadata meta3 = new AssignmentMetadata("manager", "2025-03-15 10:30:00", "Temporary access");
        System.out.println(meta3.format());
    }
}