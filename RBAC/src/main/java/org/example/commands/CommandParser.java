package org.example.commands;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandParser {

    private Map<String, Command> commands;
    private Map<String, String> commandDescriptions;

    public CommandParser() {
        this.commands = new LinkedHashMap<>();
        this.commandDescriptions = new LinkedHashMap<>();
    }

    public void registerCommand(String name, String description, Command command) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Command name cannot be null or empty");
        }
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }

        String key = name.toLowerCase().trim();
        commands.put(key, command);
        commandDescriptions.put(key, description != null ? description : "No description");
    }

    public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        if (commandName == null || commandName.trim().isEmpty()) {
            System.out.println("Error: empty command. Type 'help' for list of commands.");
            return;
        }

        String key = commandName.toLowerCase().trim();
        Command command = commands.get(key);

        if (command == null) {
            System.out.println("Unknown command: '" + commandName + "'");
            System.out.println("Type 'help' for list of available commands.");
            return;
        }

        try {
            command.execute(scanner, system);
        } catch (Exception e) {
            System.out.println("Error executing command '" + commandName + "': " + e.getMessage());
        }
    }

    public void printHelp() {
        System.out.println("\n=== AVAILABLE COMMANDS ===");

        int maxLen = commandDescriptions.keySet().stream()
                .mapToInt(String::length)
                .max()
                .orElse(10);

        String format = "  %-" + (maxLen + 2) + "s %s";

        for (Map.Entry<String, String> entry : commandDescriptions.entrySet()) {
            System.out.printf(format + "%n", entry.getKey(), entry.getValue());
        }

        System.out.println();
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        if (input == null || input.trim().isEmpty()) {
            System.out.println("Error: empty input. Type 'help' for list of commands.");
            return;
        }

        String trimmed = input.trim();
        String commandName;
        int spaceIndex = trimmed.indexOf(' ');

        if (spaceIndex == -1) {
            commandName = trimmed;
        } else {
            commandName = trimmed.substring(0, spaceIndex);
        }

        executeCommand(commandName, scanner, system);
    }
}