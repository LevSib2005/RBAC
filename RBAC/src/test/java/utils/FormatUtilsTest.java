package utils;

import org.example.utils.FormatUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FormatUtilsTest {

    @Test
    void formatTableWithEmptyHeaders() {
        String[] headers = {};
        List<String[]> rows = new ArrayList<>();
        String result = FormatUtils.formatTable(headers, rows);
        assertEquals("", result);
    }

    @Test
    void formatTableWithNullHeaders() {
        String result = FormatUtils.formatTable(null, new ArrayList<>());
        assertEquals("", result);
    }

    @Test
    void formatTableWithSingleRow() {
        String[] headers = {"Username", "Role"};
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"admin", "Administrator"});

        String result = FormatUtils.formatTable(headers, rows);

        String expected =
                "+----------+---------------+\n" +
                        "| Username | Role          |\n" +
                        "+----------+---------------+\n" +
                        "| admin    | Administrator |\n" +
                        "+----------+---------------+\n";

        assertEquals(expected, result);
    }

    @Test
    void formatTableWithMultipleRows() {
        String[] headers = {"ID", "Name"};
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"1", "John"});
        rows.add(new String[]{"2", "Jane"});
        rows.add(new String[]{"3", "Bob"});

        String result = FormatUtils.formatTable(headers, rows);

        String expected =
                "+----+------+\n" +
                        "| ID | Name |\n" +
                        "+----+------+\n" +
                        "| 1  | John |\n" +
                        "| 2  | Jane |\n" +
                        "| 3  | Bob  |\n" +
                        "+----+------+\n";

        assertEquals(expected, result);
    }

    @Test
    void formatTableWithNullValues() {
        String[] headers = {"Name", "Email", "Phone"};
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"John", "john@mail.com", null});
        rows.add(new String[]{"Jane", null, "123456"});

        String result = FormatUtils.formatTable(headers, rows);

        assertNotNull(result);
        assertTrue(result.contains("John"));
        assertTrue(result.contains("john@mail.com"));
        assertTrue(result.contains("Jane"));
        assertTrue(result.contains("123456"));
    }

    @Test
    void formatTableWithLongContent() {
        String[] headers = {"Short", "This is a very long header that should affect column width"};
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"A", "Short text"});

        String result = FormatUtils.formatTable(headers, rows);

        assertTrue(result.contains("This is a very long header that should affect column width"));
    }

    @Test
    void formatBoxWithEmptyText() {
        String result = FormatUtils.formatBox("");
        assertEquals("++\n++", result);
    }

    @Test
    void formatBoxWithNull() {
        String result = FormatUtils.formatBox(null);
        assertEquals("++\n++", result);
    }

    @Test
    void formatBoxWithSingleLine() {
        String result = FormatUtils.formatBox("Hello");

        String expected =
                "+-------+\n" +
                        "| Hello |\n" +
                        "+-------+\n";

        assertEquals(expected, result);
    }

    @Test
    void formatBoxWithMultipleLines() {
        String text = "Line 1\nLine 2\nLine 3";
        String result = FormatUtils.formatBox(text);

        String expected =
                "+--------+\n" +
                        "| Line 1 |\n" +
                        "| Line 2 |\n" +
                        "| Line 3 |\n" +
                        "+--------+\n";

        assertEquals(expected, result);
    }

    @Test
    void formatBoxWithDifferentLineLengths() {
        String text = "Short\nMuch longer line here";
        String result = FormatUtils.formatBox(text);

        assertTrue(result.contains("Much longer line here"));
    }

    @Test
    void formatHeaderWithEmptyText() {
        String result = FormatUtils.formatHeader("");
        assertEquals("\n===  ===\n", result);
    }

    @Test
    void formatHeaderWithNull() {
        String result = FormatUtils.formatHeader(null);
        assertEquals("\n===  ===\n", result);
    }

    @Test
    void formatHeaderWithText() {
        String result = FormatUtils.formatHeader("Test Header");
        assertEquals("\n=== Test Header ===\n", result);
    }

    @Test
    void truncateWithNull() {
        String result = FormatUtils.truncate(null, 10);
        assertEquals("", result);
    }

    @Test
    void truncateShorterThanMaxLength() {
        String result = FormatUtils.truncate("Hello", 10);
        assertEquals("Hello", result);
    }

    @Test
    void truncateExactLength() {
        String result = FormatUtils.truncate("Hello", 5);
        assertEquals("Hello", result);
    }

    @Test
    void truncateLongerThanMaxLength() {
        String result = FormatUtils.truncate("Hello World", 8);
        assertEquals("Hello...", result);
    }

    @Test
    void truncateWithVerySmallMaxLength() {
        String result = FormatUtils.truncate("Hello World", 2);
        assertEquals("..", result);
    }

    @Test
    void truncateWithMaxLength3() {
        String result = FormatUtils.truncate("Hello", 3);
        assertEquals("...", result);
    }

    @Test
    void padRightWithNull() {
        String result = FormatUtils.padRight(null, 5);
        assertEquals("     ", result);
    }

    @Test
    void padRightShorterThanLength() {
        String result = FormatUtils.padRight("Hi", 5);
        assertEquals("Hi   ", result);
    }

    @Test
    void padRightEqualLength() {
        String result = FormatUtils.padRight("Hello", 5);
        assertEquals("Hello", result);
    }

    @Test
    void padRightLongerThanLength() {
        String result = FormatUtils.padRight("Hello World", 5);
        assertEquals("Hello World", result);
    }

    @Test
    void padLeftWithNull() {
        String result = FormatUtils.padLeft(null, 5);
        assertEquals("     ", result);
    }

    @Test
    void padLeftShorterThanLength() {
        String result = FormatUtils.padLeft("Hi", 5);
        assertEquals("   Hi", result);
    }

    @Test
    void padLeftEqualLength() {
        String result = FormatUtils.padLeft("Hello", 5);
        assertEquals("Hello", result);
    }

    @Test
    void padLeftLongerThanLength() {
        String result = FormatUtils.padLeft("Hello World", 5);
        assertEquals("Hello World", result);
    }

    @Test
    void formatTableWithEmptyRows() {
        String[] headers = {"Header1", "Header2"};
        List<String[]> rows = new ArrayList<>();

        String result = FormatUtils.formatTable(headers, rows);

        String expected =
                "+---------+---------+\n" +
                        "| Header1 | Header2 |\n" +
                        "+---------+---------+\n" +
                        "+---------+---------+\n";

        assertEquals(expected, result);
    }

    @Test
    void formatTableWithExtraColumns() {
        String[] headers = {"Name"};
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"John", "Extra data", "More data"});

        String result = FormatUtils.formatTable(headers, rows);

        assertTrue(result.contains("John"));
        assertFalse(result.contains("Extra data"));
    }

    @Test
    void formatColumnLeftAlign() {
        String result = FormatUtils.formatColumn("text", 10, true);
        assertEquals("text      ", result);
    }

    @Test
    void formatColumnRightAlign() {
        String result = FormatUtils.formatColumn("text", 10, false);
        assertEquals("      text", result);
    }

    @Test
    void formatColumnWithNull() {
        String result = FormatUtils.formatColumn(null, 5, true);
        assertEquals("     ", result);
    }

    @Test
    void integrationTest() {
        String header = FormatUtils.formatHeader("User List");
        String[] headers = {"ID", "Username", "Email"};
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"1", "john", "john@mail.com"});
        rows.add(new String[]{"2", "jane", "jane@mail.com"});

        String table = FormatUtils.formatTable(headers, rows);

        String fullOutput = header + "\n" + table;

        assertTrue(fullOutput.contains("=== User List ==="));
        assertTrue(fullOutput.contains("john"));
        assertTrue(fullOutput.contains("jane"));
        assertTrue(fullOutput.contains("+"));
        assertTrue(fullOutput.contains("|"));
    }
}