package utils;

import org.example.utils.DateUtils;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Test
    void getCurrentDateReturnsValidFormat() {
        String currentDate = DateUtils.getCurrentDate();

        assertTrue(currentDate.matches("\\d{4}-\\d{2}-\\d{2}"));

        String expected = LocalDate.now().format(FORMATTER);
        assertEquals(expected, currentDate);
    }

    @Test
    void getCurrentDateTimeReturnsValidFormat() {
        String currentDateTime = DateUtils.getCurrentDateTime();

        assertTrue(currentDateTime.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void isBeforeWithEarlierDate() {
        assertTrue(DateUtils.isBefore("2025-01-01", "2025-01-02"));
        assertTrue(DateUtils.isBefore("2024-12-31", "2025-01-01"));
    }

    @Test
    void isBeforeWithLaterDate() {
        assertFalse(DateUtils.isBefore("2025-01-02", "2025-01-01"));
        assertFalse(DateUtils.isBefore("2025-12-31", "2025-01-01"));
    }

    @Test
    void isBeforeWithEqualDates() {
        assertFalse(DateUtils.isBefore("2025-01-01", "2025-01-01"));
    }

    @Test
    void isBeforeWithNull() {
        assertFalse(DateUtils.isBefore(null, "2025-01-01"));
        assertFalse(DateUtils.isBefore("2025-01-01", null));
        assertFalse(DateUtils.isBefore(null, null));
    }

    @Test
    void isAfterWithLaterDate() {
        assertTrue(DateUtils.isAfter("2025-01-02", "2025-01-01"));
        assertTrue(DateUtils.isAfter("2025-12-31", "2025-01-01"));
    }

    @Test
    void isAfterWithEarlierDate() {
        assertFalse(DateUtils.isAfter("2025-01-01", "2025-01-02"));
        assertFalse(DateUtils.isAfter("2024-12-31", "2025-01-01"));
    }

    @Test
    void isAfterWithEqualDates() {
        assertFalse(DateUtils.isAfter("2025-01-01", "2025-01-01"));
    }

    @Test
    void isAfterWithNull() {
        assertFalse(DateUtils.isAfter(null, "2025-01-01"));
        assertFalse(DateUtils.isAfter("2025-01-01", null));
        assertFalse(DateUtils.isAfter(null, null));
    }

    @Test
    void addDaysToValidDate() {
        assertEquals("2025-01-06", DateUtils.addDays("2025-01-01", 5));
        assertEquals("2025-02-01", DateUtils.addDays("2025-01-01", 31));
        assertEquals("2024-12-27", DateUtils.addDays("2025-01-01", -5));
    }

    @Test
    void addDaysWithLeapYear() {
        assertEquals("2024-03-01", DateUtils.addDays("2024-02-28", 2)); // 2024 високосный
        assertEquals("2025-03-01", DateUtils.addDays("2025-02-28", 1)); // 2025 не високосный
    }

    @Test
    void addDaysToYearEnd() {
        assertEquals("2026-01-01", DateUtils.addDays("2025-12-31", 1));
        assertEquals("2025-01-01", DateUtils.addDays("2024-12-31", 1));
    }

    @Test
    void addDaysWithNullDate() {
        assertNull(DateUtils.addDays(null, 5));
    }

    @Test
    void addDaysWithInvalidDate() {
        assertEquals("invalid-date", DateUtils.addDays("invalid-date", 5));
    }

    @Test
    void formatRelativeTimeToday() {
        String today = LocalDate.now().format(FORMATTER);
        assertEquals("today", DateUtils.formatRelativeTime(today));
    }

    @Test
    void formatRelativeTimeYesterday() {
        String yesterday = LocalDate.now().minusDays(1).format(FORMATTER);
        assertEquals("yesterday", DateUtils.formatRelativeTime(yesterday));
    }

    @Test
    void formatRelativeTimeTomorrow() {
        String tomorrow = LocalDate.now().plusDays(1).format(FORMATTER);
        assertEquals("tomorrow", DateUtils.formatRelativeTime(tomorrow));
    }

    @Test
    void formatRelativeTimeDaysAgo() {
        String threeDaysAgo = LocalDate.now().minusDays(3).format(FORMATTER);
        assertEquals("3 days ago", DateUtils.formatRelativeTime(threeDaysAgo));

        String oneWeekAgo = LocalDate.now().minusDays(7).format(FORMATTER);
        assertEquals("7 days ago", DateUtils.formatRelativeTime(oneWeekAgo));
    }

    @Test
    void formatRelativeTimeInFuture() {
        String inTwoDays = LocalDate.now().plusDays(2).format(FORMATTER);
        assertEquals("in 2 days", DateUtils.formatRelativeTime(inTwoDays));

        String inTenDays = LocalDate.now().plusDays(10).format(FORMATTER);
        assertEquals("in 10 days", DateUtils.formatRelativeTime(inTenDays));
    }

    @Test
    void formatRelativeTimeWithNull() {
        assertEquals("unknown date", DateUtils.formatRelativeTime(null));
    }

    @Test
    void formatRelativeTimeWithInvalidDate() {
        assertEquals("invalid", DateUtils.formatRelativeTime("invalid"));
        assertEquals("", DateUtils.formatRelativeTime(""));
    }

    @Test
    void integrationTest() {
        // Тест на совместную работу методов
        String today = DateUtils.getCurrentDate();
        String tomorrow = DateUtils.addDays(today, 1);
        String yesterday = DateUtils.addDays(today, -1);

        assertTrue(DateUtils.isBefore(yesterday, today));
        assertTrue(DateUtils.isAfter(tomorrow, today));

        assertEquals("today", DateUtils.formatRelativeTime(today));
        assertEquals("tomorrow", DateUtils.formatRelativeTime(tomorrow));
        assertEquals("yesterday", DateUtils.formatRelativeTime(yesterday));
    }

    @Test
    void isBeforeWorksWithDifferentYears() {
        assertTrue(DateUtils.isBefore("2024-12-31", "2025-01-01"));
        assertFalse(DateUtils.isBefore("2025-01-01", "2024-12-31"));
    }

    @Test
    void isAfterWorksWithDifferentYears() {
        assertTrue(DateUtils.isAfter("2025-01-01", "2024-12-31"));
        assertFalse(DateUtils.isAfter("2024-12-31", "2025-01-01"));
    }

    @Test
    void addDaysMaintainsConsistency() {
        String startDate = "2025-01-01";
        String plus5 = DateUtils.addDays(startDate, 5);
        String plus10 = DateUtils.addDays(startDate, 10);
        String plus5Plus5 = DateUtils.addDays(plus5, 5);

        assertEquals(plus10, plus5Plus5);
        assertTrue(DateUtils.isBefore(startDate, plus5));
        assertTrue(DateUtils.isBefore(plus5, plus10));
    }

    @Test
    void formatRelativeTimeConsistency() {
        String today = LocalDate.now().format(FORMATTER);
        String inThreeDays = LocalDate.now().plusDays(3).format(FORMATTER);
        String threeDaysAgo = LocalDate.now().minusDays(3).format(FORMATTER);

        assertEquals("today", DateUtils.formatRelativeTime(today));
        assertEquals("in 3 days", DateUtils.formatRelativeTime(inThreeDays));
        assertEquals("3 days ago", DateUtils.formatRelativeTime(threeDaysAgo));
    }
}