package org.example.utils;

import java.util.List;
import java.util.Scanner;

public class ConsoleUtils {

    private ConsoleUtils() {}

    public static String promptString(Scanner scanner, String message, boolean required) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();

            if (!required || !input.isEmpty()) {
                return input;
            }
            System.out.println("Error: This field cannot be empty. Please try again.");
        }
    }

    public static int promptInt(Scanner scanner, String message, int min, int max) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("Error: Please enter a number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid number.");
            }
        }
    }

    public static boolean promptYesNo(Scanner scanner, String message) {
        while (true) {
            System.out.print(message + " (yes/no): ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("yes") || input.equals("y")) {
                return true;
            }
            if (input.equals("no") || input.equals("n")) {
                return false;
            }
            System.out.println("Error: Please enter 'yes' or 'no'.");
        }
    }

    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Options list cannot be empty");
        }

        while (true) {
            System.out.println(message);
            for (int i = 0; i < options.size(); i++) {
                System.out.printf("  %d. %s%n", i + 1, options.get(i).toString());
            }
            System.out.print("Choose (1-" + options.size() + "): ");

            String input = scanner.nextLine().trim();

            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= options.size()) {
                    return options.get(choice - 1);
                }
                System.out.printf("Error: Please enter a number between 1 and %d.%n", options.size());
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid number.");
            }
        }
    }
}