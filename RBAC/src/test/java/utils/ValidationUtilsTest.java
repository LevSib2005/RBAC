package utils;

import org.example.utils.ValidationUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {
    @Test
    void isValidUsername_ValidUsername_ReturnsTrue() {
        assertTrue(ValidationUtils.isValidUsername("john_doe"));
        assertTrue(ValidationUtils.isValidUsername("user123"));
        assertTrue(ValidationUtils.isValidUsername("a1_b2_c3"));
        assertTrue(ValidationUtils.isValidUsername("admin"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "jo",
            "very_long_username_123",
            "user@name",
            "user-name",
            "user.name",
            "user name",
            ""
    })
    void isValidUsername_InvalidUsername_ReturnsFalse(String username) {
        assertFalse(ValidationUtils.isValidUsername(username));
    }

    @Test
    void isValidUsername_NullUsername_ReturnsFalse() {
        assertFalse(ValidationUtils.isValidUsername(null));
    }

    @Test
    void isValidEmail_ValidEmail_ReturnsTrue() {
        assertTrue(ValidationUtils.isValidEmail("user@example.com"));
        assertTrue(ValidationUtils.isValidEmail("user.name@example.co.uk"));
        assertTrue(ValidationUtils.isValidEmail("user+tag@example.com"));
        assertTrue(ValidationUtils.isValidEmail("user123@example.org"));
        assertTrue(ValidationUtils.isValidEmail("admin@system.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "userexample.com",
            "user@example",
            "@example.com",
            "user@.com",
            "user@example.",
            "user@exam ple.com",
            "user name@example.com",
            ""
    })

    void isValidEmail_InvalidEmail_ReturnsFalse(String email) {
        assertFalse(ValidationUtils.isValidEmail(email));
    }

    @Test
    void isValidEmail_NullEmail_ReturnsFalse() {
        assertFalse(ValidationUtils.isValidEmail(null));
    }

    @Test
    void isValidDate_ValidDate_ReturnsTrue() {
        assertTrue(ValidationUtils.isValidDate("2025-03-10 14:30"));
        assertTrue(ValidationUtils.isValidDate("2024-12-31 23:59"));
        assertTrue(ValidationUtils.isValidDate("2025-01-01 00:00"));
        assertTrue(ValidationUtils.isValidDate("2025-02-28 12:00"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2025-03-10",
            "10-03-2025 14:30",
            "2025/03/10 14:30",
            "2025-13-10 14:30",
            "2025-03-32 14:30",
            "2025-03-10 25:30",
            "2025-03-10 14:75",
            "not a date",
            ""
    })
    void isValidDate_InvalidDate_ReturnsFalse(String date) {
        assertFalse(ValidationUtils.isValidDate(date));
    }

    @Test
    void isValidDate_NullDate_ReturnsFalse() {
        assertFalse(ValidationUtils.isValidDate(null));
    }

    @Test
    void normalizeString_WithSpaces_ReturnsTrimmedAndSingleSpaced() {
        assertEquals("John Doe", ValidationUtils.normalizeString("  John   Doe  "));
        assertEquals("hello world", ValidationUtils.normalizeString("hello   world"));
        assertEquals("test", ValidationUtils.normalizeString("  test  "));
        assertEquals("a b c", ValidationUtils.normalizeString("a   b   c"));
    }

    @Test
    void normalizeString_AlreadyNormalized_ReturnsSame() {
        // Проверка, что нормализованная строка не меняется
        assertEquals("John Doe", ValidationUtils.normalizeString("John Doe"));
        assertEquals("hello", ValidationUtils.normalizeString("hello"));
    }

    @Test
    void normalizeString_EmptyString_ReturnsEmpty() {
        assertEquals("", ValidationUtils.normalizeString(""));
        assertEquals("", ValidationUtils.normalizeString("   "));
    }

    @Test
    void normalizeString_NullInput_ReturnsNull() {
        assertNull(ValidationUtils.normalizeString(null));
    }

    @Test
    void requireNonEmpty_ValidValue_DoesNotThrow() {
        assertDoesNotThrow(() -> ValidationUtils.requireNonEmpty("test", "Field"));
        assertDoesNotThrow(() -> ValidationUtils.requireNonEmpty("  hello  ", "Field"));
    }

    @Test
    void requireNonEmpty_NullValue_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtils.requireNonEmpty(null, "Username")
        );
        assertEquals("Username cannot be null or empty", exception.getMessage());
    }

    @Test
    void requireNonEmpty_EmptyValue_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtils.requireNonEmpty("", "Email")
        );
        assertEquals("Email cannot be null or empty", exception.getMessage());
    }

    @Test
    void requireNonEmpty_BlankValue_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtils.requireNonEmpty("   ", "Password")
        );
        assertEquals("Password cannot be null or empty", exception.getMessage());
    }

    @Test
    void isValidUsername_EdgeCases() {
        assertTrue(ValidationUtils.isValidUsername("abc"));
        assertTrue(ValidationUtils.isValidUsername("abcdefghijklmnopqrst"));
        assertFalse(ValidationUtils.isValidUsername("ab"));
        assertFalse(ValidationUtils.isValidUsername("abcdefghijklmnopqrstu"));
    }

    @Test
    void isValidEmail_EdgeCases() {
        assertTrue(ValidationUtils.isValidEmail("very.common@example.com"));
        assertTrue(ValidationUtils.isValidEmail("disposable.style.email.with+symbol@example.com"));
        assertTrue(ValidationUtils.isValidEmail("other.email-with-hyphen@example.com"));
        assertTrue(ValidationUtils.isValidEmail("fully-qualified-domain@example.com"));
    }

    @Test
    void isValidDate_LeapYear() {
        assertTrue(ValidationUtils.isValidDate("2024-02-29 12:00"));
        assertFalse(ValidationUtils.isValidDate("2025-02-29 12:00"));
    }
}