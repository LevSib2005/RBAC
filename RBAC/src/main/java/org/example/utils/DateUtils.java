package org.example.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateUtils() {}

    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    public static boolean isBefore(String date1, String date2) {
        if (date1 == null || date2 == null) return false;
        return date1.compareTo(date2) < 0;
    }

    public static boolean isAfter(String date1, String date2) {
        if (date1 == null || date2 == null) return false;
        return date1.compareTo(date2) > 0;
    }

    public static String addDays(String date, int days) {
        if (date == null) return null;

        try {
            LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);
            LocalDate newDate = localDate.plusDays(days);
            return newDate.format(DATE_FORMATTER);
        } catch (Exception e) {
            return date;
        }
    }

    public static String formatRelativeTime(String date) {
        if (date == null) return "unknown date";

        try {
            LocalDate targetDate = LocalDate.parse(date, DATE_FORMATTER);
            LocalDate now = LocalDate.now();

            long daysDiff = ChronoUnit.DAYS.between(targetDate, now);

            if (daysDiff == 0) {
                return "today";
            } else if (daysDiff == 1) {
                return "yesterday";
            } else if (daysDiff == -1) {
                return "tomorrow";
            } else if (daysDiff > 0) {
                return daysDiff + " days ago";
            } else {
                return "in " + Math.abs(daysDiff) + " days";
            }
        } catch (Exception e) {
            return date;
        }
    }
}