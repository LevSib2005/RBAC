package utils;

import org.example.utils.AuditEntry;
import org.example.utils.AuditLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
    }

    @Test
    void newAuditLogShouldBeEmpty() {
        List<AuditEntry> entries = auditLog.getAll();
        assertTrue(entries.isEmpty());
    }

    @Test
    void logShouldAddEntry() {
        auditLog.log("СОЗДАНИЕ", "admin", "Пользователь", "Создан пользователь john");

        List<AuditEntry> entries = auditLog.getAll();
        assertEquals(1, entries.size());

        AuditEntry entry = entries.get(0);
        assertEquals("СОЗДАНИЕ", entry.action());
        assertEquals("admin", entry.performer());
        assertEquals("Пользователь", entry.target());
        assertEquals("Создан пользователь john", entry.details());
        assertNotNull(entry.timestamp());
    }

    @Test
    void logShouldAddMultipleEntries() {
        auditLog.log("СОЗДАНИЕ", "admin", "Пользователь", "Создан john");
        auditLog.log("УДАЛЕНИЕ", "admin", "Пользователь", "Удален jane");
        auditLog.log("НАЗНАЧЕНИЕ", "manager", "Роль", "Роль назначена bob");

        assertEquals(3, auditLog.getAll().size());
    }

    @Test
    void logShouldAddEntriesWithCurrentTimestamp() {
        auditLog.log("ТЕСТ", "user", "target", "details");

        AuditEntry entry = auditLog.getAll().get(0);
        String timestamp = entry.timestamp();

        assertTrue(timestamp.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void getAllShouldReturnCopyOfEntries() {
        auditLog.log("ТЕСТ", "user", "target", "details");

        List<AuditEntry> entries1 = auditLog.getAll();
        List<AuditEntry> entries2 = auditLog.getAll();

        assertNotSame(entries1, entries2);

        assertEquals(entries1.size(), entries2.size());
        assertEquals(entries1.get(0), entries2.get(0));
    }

    @Test
    void getByPerformerShouldReturnOnlyEntriesForSpecificPerformer() {
        auditLog.log("СОЗДАНИЕ", "admin", "Пользователь", "Создан john");
        auditLog.log("УДАЛЕНИЕ", "admin", "Пользователь", "Удален jane");
        auditLog.log("НАЗНАЧЕНИЕ", "manager", "Роль", "Роль назначена bob");
        auditLog.log("СОЗДАНИЕ", "user1", "Роль", "Создана роль");

        List<AuditEntry> adminEntries = auditLog.getByPerformer("admin");

        assertEquals(2, adminEntries.size());
        assertTrue(adminEntries.stream().allMatch(e -> e.performer().equals("admin")));
    }

    @Test
    void getByPerformerShouldBeCaseInsensitive() {
        auditLog.log("СОЗДАНИЕ", "Admin", "Пользователь", "Создан john");

        List<AuditEntry> entries = auditLog.getByPerformer("admin");
        assertEquals(1, entries.size());

        entries = auditLog.getByPerformer("ADMIN");
        assertEquals(1, entries.size());
    }

    @Test
    void getByPerformerWithNoMatchesShouldReturnEmptyList() {
        auditLog.log("СОЗДАНИЕ", "admin", "Пользователь", "Создан john");

        List<AuditEntry> entries = auditLog.getByPerformer("nonexistent");
        assertTrue(entries.isEmpty());
    }

    @Test
    void getByActionShouldReturnOnlyEntriesWithSpecificAction() {
        auditLog.log("СОЗДАНИЕ", "admin", "Пользователь", "Создан john");
        auditLog.log("УДАЛЕНИЕ", "admin", "Пользователь", "Удален jane");
        auditLog.log("НАЗНАЧЕНИЕ", "manager", "Роль", "Роль назначена bob");
        auditLog.log("СОЗДАНИЕ", "user1", "Роль", "Создана роль");

        List<AuditEntry> createEntries = auditLog.getByAction("СОЗДАНИЕ");

        assertEquals(2, createEntries.size());
        assertTrue(createEntries.stream().allMatch(e -> e.action().equals("СОЗДАНИЕ")));
    }

    @Test
    void getByActionShouldBeCaseInsensitive() {
        auditLog.log("создание", "admin", "Пользователь", "Создан john");

        List<AuditEntry> entries = auditLog.getByAction("СОЗДАНИЕ");
        assertEquals(1, entries.size());
    }

    @Test
    void getByActionWithNoMatchesShouldReturnEmptyList() {
        auditLog.log("СОЗДАНИЕ", "admin", "Пользователь", "Создан john");

        List<AuditEntry> entries = auditLog.getByAction("НЕСУЩЕСТВУЕТ");
        assertTrue(entries.isEmpty());
    }

    @Test
    void printLogShouldNotThrowExceptionWhenEmpty(@TempDir Path tempDir) {
        assertDoesNotThrow(() -> auditLog.printLog());
    }

    @Test
    void printLogShouldNotThrowExceptionWithEntries(@TempDir Path tempDir) {
        auditLog.log("ТЕСТ", "user", "target", "details");
        assertDoesNotThrow(() -> auditLog.printLog());
    }

    @Test
    void saveToFileShouldCreateFileWithEntries(@TempDir Path tempDir) throws IOException {
        Path filePath = tempDir.resolve("audit.log");
        String filename = filePath.toString();

        auditLog.log("СОЗДАНИЕ", "admin", "Пользователь", "Создан john");
        auditLog.log("УДАЛЕНИЕ", "admin", "Пользователь", "Удален jane");

        auditLog.saveToFile(filename);

        assertTrue(filePath.toFile().exists());

        List<String> lines = java.nio.file.Files.readAllLines(filePath);

        assertEquals(3, lines.size());

        assertTrue(lines.get(2).contains("Всего записей: 2"));
    }

    @Test
    void saveToFileWithEmptyLogShouldCreateFileWithOnlyTotal(@TempDir Path tempDir) throws IOException {
        Path filePath = tempDir.resolve("empty.log");
        auditLog.saveToFile(filePath.toString());

        List<String> lines = java.nio.file.Files.readAllLines(filePath);

        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("Всего записей: 0"));
    }

    @Test
    void saveToFileWithInvalidPathShouldHandleException() {
        assertDoesNotThrow(() -> auditLog.saveToFile("/invalid/path/audit.log"));
    }

    @Test
    void multipleOperationsShouldWorkCorrectly() {
        auditLog.log("СОЗДАНИЕ", "admin", "Пользователь", "Создан john");
        auditLog.log("СОЗДАНИЕ", "admin", "Роль", "Создана роль Admin");
        auditLog.log("УДАЛЕНИЕ", "admin", "Пользователь", "Удален jane");
        auditLog.log("НАЗНАЧЕНИЕ", "manager", "Пользователь", "Роль назначена bob");

        assertEquals(4, auditLog.getAll().size());

        assertEquals(2, auditLog.getByAction("СОЗДАНИЕ").size());

        assertEquals(3, auditLog.getByPerformer("admin").size());
        assertEquals(1, auditLog.getByPerformer("manager").size());

        List<AuditEntry> adminCreateEntries = auditLog.getByPerformer("admin").stream()
                .filter(e -> e.action().equals("СОЗДАНИЕ"))
                .collect(Collectors.toList());
        assertEquals(2, adminCreateEntries.size());
    }
}