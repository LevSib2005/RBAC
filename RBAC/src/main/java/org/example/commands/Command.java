package org.example.commands;

import org.example.commands.RBACSystem;
import java.util.Scanner;

@FunctionalInterface
public interface Command {
    void execute(Scanner scanner, RBACSystem system);
}