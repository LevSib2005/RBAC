package utils;

import org.example.utils.ConsoleUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleUtilsTest {

    private Scanner scanner;
    private InputStream originalSystemIn;

    @BeforeEach
    void setUp() {
        originalSystemIn = System.in;
    }

    private void provideInput(String data) {
        System.setIn(new ByteArrayInputStream(data.getBytes()));
        scanner = new Scanner(System.in);
    }

    @Test
    void promptStringRequiredValidInput() {
        provideInput("test\n");
        String result = ConsoleUtils.promptString(scanner, "Enter name: ", true);
        assertEquals("test", result);
    }

    @Test
    void promptStringRequiredEmptyInputThenValid() {
        provideInput("\n\ntest\n");
        String result = ConsoleUtils.promptString(scanner, "Enter name: ", true);
        assertEquals("test", result);
    }

    @Test
    void promptStringNotRequiredEmptyInput() {
        provideInput("\n");
        String result = ConsoleUtils.promptString(scanner, "Enter name (optional): ", false);
        assertEquals("", result);
    }

    @Test
    void promptStringNotRequiredWithInput() {
        provideInput("hello\n");
        String result = ConsoleUtils.promptString(scanner, "Enter name: ", false);
        assertEquals("hello", result);
    }

    @Test
    void promptStringTrimsWhitespace() {
        provideInput("  test  \n");
        String result = ConsoleUtils.promptString(scanner, "Enter name: ", true);
        assertEquals("test", result);
    }

    @Test
    void promptIntValidInput() {
        provideInput("5\n");
        int result = ConsoleUtils.promptInt(scanner, "Enter number: ", 1, 10);
        assertEquals(5, result);
    }

    @Test
    void promptIntInvalidNumberThenValid() {
        provideInput("abc\n5\n");
        int result = ConsoleUtils.promptInt(scanner, "Enter number: ", 1, 10);
        assertEquals(5, result);
    }

    @Test
    void promptIntOutOfRangeThenValid() {
        provideInput("15\n5\n");
        int result = ConsoleUtils.promptInt(scanner, "Enter number: ", 1, 10);
        assertEquals(5, result);
    }

    @Test
    void promptIntMinValue() {
        provideInput("1\n");
        int result = ConsoleUtils.promptInt(scanner, "Enter number: ", 1, 10);
        assertEquals(1, result);
    }

    @Test
    void promptIntMaxValue() {
        provideInput("10\n");
        int result = ConsoleUtils.promptInt(scanner, "Enter number: ", 1, 10);
        assertEquals(10, result);
    }

    @Test
    void promptIntMultipleInvalidAttempts() {
        provideInput("abc\nxyz\n0\n15\n7\n");
        int result = ConsoleUtils.promptInt(scanner, "Enter number: ", 1, 10);
        assertEquals(7, result);
    }

    @Test
    void promptIntWithNegativeRange() {
        provideInput("-5\n");
        int result = ConsoleUtils.promptInt(scanner, "Enter number: ", -10, 0);
        assertEquals(-5, result);
    }

    @Test
    void promptYesNoYesInput() {
        provideInput("yes\n");
        assertTrue(ConsoleUtils.promptYesNo(scanner, "Continue?"));
    }

    @Test
    void promptYesNoYInput() {
        provideInput("y\n");
        assertTrue(ConsoleUtils.promptYesNo(scanner, "Continue?"));
    }

    @Test
    void promptYesNoNoInput() {
        provideInput("no\n");
        assertFalse(ConsoleUtils.promptYesNo(scanner, "Continue?"));
    }

    @Test
    void promptYesNoNInput() {
        provideInput("n\n");
        assertFalse(ConsoleUtils.promptYesNo(scanner, "Continue?"));
    }

    @Test
    void promptYesNoInvalidThenValid() {
        provideInput("maybe\nyes\n");
        assertTrue(ConsoleUtils.promptYesNo(scanner, "Continue?"));
    }

    @Test
    void promptYesNoCaseInsensitive() {
        provideInput("YES\n");
        assertTrue(ConsoleUtils.promptYesNo(scanner, "Continue?"));
    }

    @Test
    void promptYesNoMultipleInvalidAttempts() {
        provideInput("maybe\nok\nsure\ny\n");
        assertTrue(ConsoleUtils.promptYesNo(scanner, "Continue?"));
    }

    @Test
    void promptChoiceValidSelection() {
        List<String> options = List.of("Option A", "Option B", "Option C");
        provideInput("2\n");

        String result = ConsoleUtils.promptChoice(scanner, "Select option:", options);
        assertEquals("Option B", result);
    }

    @Test
    void promptChoiceFirstOption() {
        List<String> options = List.of("First", "Second", "Third");
        provideInput("1\n");

        String result = ConsoleUtils.promptChoice(scanner, "Select:", options);
        assertEquals("First", result);
    }

    @Test
    void promptChoiceLastOption() {
        List<String> options = List.of("First", "Second", "Third");
        provideInput("3\n");

        String result = ConsoleUtils.promptChoice(scanner, "Select:", options);
        assertEquals("Third", result);
    }

    @Test
    void promptChoiceInvalidNumberThenValid() {
        List<String> options = List.of("A", "B", "C");
        provideInput("5\n2\n");

        String result = ConsoleUtils.promptChoice(scanner, "Select:", options);
        assertEquals("B", result);
    }

    @Test
    void promptChoiceInvalidInputThenValid() {
        List<String> options = List.of("A", "B", "C");
        provideInput("abc\n2\n");

        String result = ConsoleUtils.promptChoice(scanner, "Select:", options);
        assertEquals("B", result);
    }

    @Test
    void promptChoiceWithIntegerOptions() {
        List<Integer> options = List.of(10, 20, 30, 40);
        provideInput("3\n");

        Integer result = ConsoleUtils.promptChoice(scanner, "Select number:", options);
        assertEquals(30, result);
    }

    @Test
    void promptChoiceEmptyListThrowsException() {
        List<String> emptyList = List.of();
        assertThrows(IllegalArgumentException.class,
                () -> ConsoleUtils.promptChoice(scanner, "Select:", emptyList));
    }

    @Test
    void promptChoiceNullListThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> ConsoleUtils.promptChoice(scanner, "Select:", null));
    }

    @Test
    void promptChoiceMultipleInvalidAttempts() {
        List<String> options = List.of("Red", "Green", "Blue");
        provideInput("0\n4\nabc\n2\n");

        String result = ConsoleUtils.promptChoice(scanner, "Choose color:", options);
        assertEquals("Green", result);
    }

    @Test
    void promptChoiceSingleOption() {
        List<String> options = List.of("Only choice");
        provideInput("1\n");

        String result = ConsoleUtils.promptChoice(scanner, "Select:", options);
        assertEquals("Only choice", result);
    }

    @Test
    void promptIntAllowsZero() {
        provideInput("0\n");
        int result = ConsoleUtils.promptInt(scanner, "Enter number:", 0, 5);
        assertEquals(0, result);
    }

    @Test
    void promptIntAllowsNegative() {
        provideInput("-3\n");
        int result = ConsoleUtils.promptInt(scanner, "Enter number:", -5, 5);
        assertEquals(-3, result);
    }

    @Test
    void promptStringRequiredIgnoresWhitespaceOnly() {
        provideInput("   \ntest\n");
        String result = ConsoleUtils.promptString(scanner, "Enter:", true);
        assertEquals("test", result);
    }

    @Test
    void promptYesNoWithWhitespace() {
        provideInput("  yes  \n");
        assertTrue(ConsoleUtils.promptYesNo(scanner, "Confirm?"));
    }

    @Test
    void promptIntWithWhitespace() {
        provideInput("  42  \n");
        int result = ConsoleUtils.promptInt(scanner, "Enter:", 1, 100);
        assertEquals(42, result);
    }

    @Test
    void promptChoiceWithWhitespace() {
        List<String> options = List.of("One", "Two", "Three");
        provideInput("  2  \n");

        String result = ConsoleUtils.promptChoice(scanner, "Pick:", options);
        assertEquals("Two", result);
    }
}