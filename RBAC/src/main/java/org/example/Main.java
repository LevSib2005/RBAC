package org.example;


import org.example.commands.CommandParser;
import org.example.commands.CommandRegistry;
import org.example.commands.RBACSystem;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== RBAC System ===");
        System.out.println("Type 'help' for available commands.");

        RBACSystem system = new RBACSystem();
        system.initialize();
        system.startPeriodicTask(60);

        CommandParser parser = new CommandParser();
        CommandRegistry.registerAll(parser);

        Scanner scanner = new Scanner(System.in);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down...");
            system.shutdown();
        }));

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine();
            if (input == null) {
                break;
            }
            parser.parseAndExecute(input, scanner, system);
        }
    }
}