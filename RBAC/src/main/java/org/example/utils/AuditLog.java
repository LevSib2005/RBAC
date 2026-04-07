package org.example.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AuditLog {

    private List<AuditEntry> entries;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AuditLog() {
        this.entries = new ArrayList<>();
    }

    public void log(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        AuditEntry entry = new AuditEntry(timestamp, action, performer, target, details);
        entries.add(entry);
    }

    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    public List<AuditEntry> getByPerformer(String performer) {
        return entries.stream()
                .filter(e -> e.performer().equalsIgnoreCase(performer))
                .collect(Collectors.toList());
    }

    public List<AuditEntry> getByAction(String action) {
        return entries.stream()
                .filter(e -> e.action().equalsIgnoreCase(action))
                .collect(Collectors.toList());
    }

    public void printLog() {
        if (entries.isEmpty()) {
            System.out.println("Записей нет");
            return;
        }

        for (AuditEntry entry : entries) {
            System.out.printf("[%s] %s | Исполнитель: %s | Цель: %s | %s%n",
                    entry.timestamp(), entry.action(), entry.performer(), entry.target(), entry.details());
        }
        System.out.println("Всего записей: " + entries.size());
    }

    public void saveToFile(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (AuditEntry entry : entries) {
                writer.printf("[%s] %s | Исполнитель: %s | Цель: %s | %s%n",
                        entry.timestamp(), entry.action(), entry.performer(), entry.target(), entry.details());
            }
            writer.println("Всего записей: " + entries.size());
            System.out.println("Лог сохранен в файл: " + filename);
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении лога: " + e.getMessage());
        }
    }

    public void clear() {
        entries.clear();
        log("CLEAR", "system", "audit-log", "Audit log was cleared");
    }
}