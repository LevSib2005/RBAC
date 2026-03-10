package org.example.utils;
import java.util.List;

public class FormatUtils {

    private FormatUtils() {}

    public static String formatTable(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) {
            return "";
        }

        int[] columnWidths = new int[headers.length];

        for (int i = 0; i < headers.length; i++) {
            columnWidths[i] = headers[i].length();
        }

        for (String[] row : rows) {
            for (int i = 0; i < Math.min(row.length, columnWidths.length); i++) {
                if (row[i] != null && row[i].length() > columnWidths[i]) {
                    columnWidths[i] = row[i].length();
                }
            }
        }

        for (int i = 0; i < columnWidths.length; i++) {
            columnWidths[i] += 2;
        }

        StringBuilder table = new StringBuilder();

        table.append("+");
        for (int width : columnWidths) {
            table.append("-".repeat(width)).append("+");
        }
        table.append("\n");

        table.append("|");
        for (int i = 0; i < headers.length; i++) {
            String header = " " + headers[i] + " ";
            table.append(padRight(header, columnWidths[i])).append("|");
        }
        table.append("\n");

        table.append("+");
        for (int width : columnWidths) {
            table.append("-".repeat(width)).append("+");
        }
        table.append("\n");

        for (String[] row : rows) {
            table.append("|");
            for (int i = 0; i < columnWidths.length; i++) {
                String cell = "";
                if (i < row.length && row[i] != null) {
                    cell = " " + row[i] + " ";
                } else {
                    cell = " ".repeat(columnWidths[i]);
                }
                table.append(padRight(cell, columnWidths[i])).append("|");
            }
            table.append("\n");
        }

        table.append("+");
        for (int width : columnWidths) {
            table.append("-".repeat(width)).append("+");
        }
        table.append("\n");

        return table.toString();
    }

    public static String formatBox(String text) {
        if (text == null || text.isEmpty()) {
            return "++\n++";
        }

        String[] lines = text.split("\n");
        int maxLength = 0;
        for (String line : lines) {
            maxLength = Math.max(maxLength, line.length());
        }

        StringBuilder box = new StringBuilder();

        box.append("+").append("-".repeat(maxLength + 2)).append("+\n");

        for (String line : lines) {
            box.append("| ").append(padRight(line, maxLength)).append(" |\n");
        }

        box.append("+").append("-".repeat(maxLength + 2)).append("+\n");

        return box.toString();
    }

    public static String formatHeader(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return "\n=== " + text + " ===\n";
    }

    public static String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        if (maxLength <= 3) {
            return ".".repeat(maxLength);
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    public static String padRight(String text, int length) {
        if (text == null) {
            return " ".repeat(length);
        }
        if (text.length() >= length) {
            return text;
        }
        return text + " ".repeat(length - text.length());
    }

    public static String padLeft(String text, int length) {
        if (text == null) {
            return " ".repeat(length);
        }
        if (text.length() >= length) {
            return text;
        }
        return " ".repeat(length - text.length()) + text;
    }

    public static String formatColumn(String text, int width, boolean alignLeft) {
        if (alignLeft) {
            return padRight(text, width);
        } else {
            return padLeft(text, width);
        }
    }
}