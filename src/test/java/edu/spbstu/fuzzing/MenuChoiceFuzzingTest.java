package edu.spbstu.fuzzing;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import edu.spbstu.menu.IOAdapter;
import edu.spbstu.menu.Menu;
import edu.spbstu.menu.StreamIOAdapter;
import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class MenuChoiceFuzzingTest {
    @FuzzTest(maxDuration = "15m")
    public void fuzzTest(FuzzedDataProvider data) throws IOException {
        int max = data.consumeInt();
        String s = data.consumeAsciiString(100);

        IOAdapter mockIO = mock(IOAdapter.class);

        doNothing().when(mockIO).print(anyString());
        doNothing().when(mockIO).printLine(anyString());
        when(mockIO.readLine()).thenReturn(s);

        Menu menu = new Menu(mockIO);

        try {
            boolean valid;
            int expected;
            try {
                if (s != null) {
                    int choice = Integer.parseInt(s);
                    valid = Pattern.matches("[+-]?\\d+", s) && choice >= 1 && choice <= max;
                    expected = valid ? choice : -1;
                } else {
                    expected = -1;
                }
                assertEquals(expected, menu.getChoiceOnce(s, max));
            } catch (NumberFormatException e) {
                assertThrows(NumberFormatException.class, () -> menu.getChoiceOnce(s, max));
            }
        } catch (AssertionFailedError e) {
            System.out.println("max: " + max);
            System.out.println("input: " + s);
            throw e;
        }
    }

}