package edu.spbstu;

import edu.spbstu.menu.StreamIOAdapter;
import org.junit.jupiter.api.*;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class StreamIOAdapterTest {

    private ByteArrayOutputStream out;
    private ByteArrayInputStream in;
    private StreamIOAdapter adapter;

    @BeforeEach
    void setUp() {
        out = new ByteArrayOutputStream();
        in = new ByteArrayInputStream(new byte[0]); // empty by default
        adapter = new StreamIOAdapter(out, in);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Ensure adapter is closed after each test
        adapter.close();
    }

    @Test
    void print_writesExactBytesToOutput() throws IOException {
        String message = "Hello, World!";
        adapter.print(message);
        String result = out.toString();
        assertEquals(message, result);
    }

    @Test
    void printLine_appendsLineSeparator() throws IOException {
        String message = "Line";
        adapter.printLine(message);
        String expected = message + System.lineSeparator();
        String result = out.toString();
        assertEquals(expected, result);
    }

    @Test
    void readLine_returnsLinesCorrectly() throws IOException {
        String inputContent = "FirstLine\nSecondLine\n";
        in = new ByteArrayInputStream(inputContent.getBytes());
        adapter = new StreamIOAdapter(out, in);

        String line1 = adapter.readLine();
        String line2 = adapter.readLine();
        String line3 = adapter.readLine();

        assertEquals("FirstLine", line1);
        assertEquals("SecondLine", line2);
        assertNull(line3);
    }

    @Test
    void close_closesStreams_soReadThrowsIOException() throws Exception {
        String inputContent = "Data\n";
        in = new ByteArrayInputStream(inputContent.getBytes());
        adapter = new StreamIOAdapter(out, in);

        // First, read one line successfully
        assertEquals("Data", adapter.readLine());

        // Now close adapter
        adapter.close();

        // After closing, readLine should throw IOException
        assertThrows(IOException.class, () -> adapter.readLine());
    }
}

