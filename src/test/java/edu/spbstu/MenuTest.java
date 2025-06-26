package edu.spbstu;

import edu.spbstu.menu.HelpInfoProvider;
import edu.spbstu.menu.Menu;
import edu.spbstu.menu.StreamIOAdapter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MenuTest {

    private ByteArrayOutputStream out;
    private ByteArrayInputStream in;
    private StreamIOAdapter ioAdapter;
    private Menu menu;

    @BeforeEach
    void setUp() {
        out = new ByteArrayOutputStream();
        // default input; will be reinitialized in each test as needed
        in = new ByteArrayInputStream(new byte[0]);
        ioAdapter = new StreamIOAdapter(out, in);
        menu = new Menu(ioAdapter);
    }

    @AfterEach
    void tearDown() throws Exception {
        ioAdapter.close();
    }

    @Test
    void displayMain_validChoice_returnsCorrectEnum_andPrintsMenu() throws IOException {
        String userInput = "2\n"; // valid choice: ALGORITHM
        in = new ByteArrayInputStream(userInput.getBytes(StandardCharsets.UTF_8));
        ioAdapter = new StreamIOAdapter(out, in);
        menu = new Menu(ioAdapter);

        Menu.MainMenuChoice choice = menu.displayMain();

        String printed = out.toString(StandardCharsets.UTF_8);
        assertThat(printed, containsString("Главное меню:"));
        assertEquals(Menu.MainMenuChoice.ALGORITHM, choice);
    }

    @Test
    void displayMain_invalidThenValidChoice_loopsUntilValid() throws IOException {
        // First input: "abc" (invalid), second: "7" (out of range), third: "3"
        String userInput = "abc\n7\n3\n";
        in = new ByteArrayInputStream(userInput.getBytes(StandardCharsets.UTF_8));
        ioAdapter = new StreamIOAdapter(out, in);
        menu = new Menu(ioAdapter);

        Menu.MainMenuChoice choice = menu.displayMain();

        String printed = out.toString(StandardCharsets.UTF_8);
        // Should contain MAIN_MENU once, then two error prompts, then accept
        assertThat(printed, containsString("Главное меню:"));
        assertThat(printed, containsString("Некорректный ввод. Пожалуйста, введите число от 1 до 5."));
        assertThat(printed, containsString("Пожалуйста, введите число от 1 до 5."));
        assertEquals(Menu.MainMenuChoice.OUTPUT, choice); // 3 -> OUTPUT
    }

    @Test
    void displayHelp_callsProviderAndPrints_usingSpy() throws IOException {
        // Create a real HelpInfoProvider implementation
        HelpInfoProvider realProvider = new HelpInfoProvider() {
            @Override
            public String getHelpInfo() {
                return "TEST_HELP_TEXT";
            }
        };
        // Spy on that real instance
        HelpInfoProvider spyProvider = Mockito.spy(realProvider);

        menu.displayHelp(spyProvider);

        String printed = out.toString(StandardCharsets.UTF_8);
        assertThat(printed.trim(), is("TEST_HELP_TEXT"));
        // Verify that getHelpInfo() was indeed called on the spy
        verify(spyProvider, times(1)).getHelpInfo();
    }

    @Test
    void displayRecommendations_printsAllLines_andWaitsForEnter() throws IOException {
        // Prepare recommendations and simulate user pressing ENTER once
        String[] recs = {"First", "Second"};
        in = new ByteArrayInputStream("\n".getBytes(StandardCharsets.UTF_8));
        ioAdapter = new StreamIOAdapter(out, in);
        menu = new Menu(ioAdapter);

        menu.displayRecommendations(recs);

        String printed = out.toString(StandardCharsets.UTF_8);
        String[] lines = printed.split(System.lineSeparator());
        assertThat(lines[0], is("Рекомендации:"));
        assertThat(lines[1], is("- First"));
        assertThat(lines[2], is("- Second"));
        // After printing, readLine() was called, so no additional output needed
    }

    static Stream<Arguments> inputChoiceProvider() {
        return Stream.of(
                Arguments.of("1\n", Menu.InputChoice.MANUAL),
                Arguments.of("2\n", Menu.InputChoice.JSON_FILE),
                Arguments.of("3\n", Menu.InputChoice.JSON)
        );
    }

    @ParameterizedTest
    @MethodSource("inputChoiceProvider")
    void getInputChoice_validInputs_selectCorrectEnum(String userInput, Menu.InputChoice expected) throws IOException {
        in = new ByteArrayInputStream(userInput.getBytes(StandardCharsets.UTF_8));
        ioAdapter = new StreamIOAdapter(out, in);
        menu = new Menu(ioAdapter);

        Menu.InputChoice choice = menu.getInputChoice();

        String printed = out.toString(StandardCharsets.UTF_8);
        assertThat(printed, containsString("Выберите способ ввода данных:"));
        assertEquals(expected, choice);
    }

    @Test
    void getInputChoice_invalidThenValid_loopsUntilValid() throws IOException {
        String userInput = "0\nabc\n2\n";
        in = new ByteArrayInputStream(userInput.getBytes(StandardCharsets.UTF_8));
        ioAdapter = new StreamIOAdapter(out, in);
        menu = new Menu(ioAdapter);

        Menu.InputChoice choice = menu.getInputChoice();

        String printed = out.toString(StandardCharsets.UTF_8);
        assertThat(printed, containsString("Выберите способ ввода данных:"));
        assertThat(printed, containsString("Некорректный ввод. Пожалуйста, введите число от 1 до 3."));
        assertThat(printed, containsString("Пожалуйста, введите число от 1 до 3."));
        assertEquals(Menu.InputChoice.JSON_FILE, choice);
    }

    static Stream<Arguments> algorithmChoiceProvider() {
        return Stream.of(
                Arguments.of("1\n", Menu.AlgorithmChoice.DSATUR),
                Arguments.of("2\n", Menu.AlgorithmChoice.RFL)
        );
    }

    @ParameterizedTest
    @MethodSource("algorithmChoiceProvider")
    void getAlgorithmChoice_validInputs_selectCorrectEnum(String userInput, Menu.AlgorithmChoice expected) throws IOException {
        in = new ByteArrayInputStream(userInput.getBytes(StandardCharsets.UTF_8));
        ioAdapter = new StreamIOAdapter(out, in);
        menu = new Menu(ioAdapter);

        Menu.AlgorithmChoice choice = menu.getAlgorithmChoice();

        String printed = out.toString(StandardCharsets.UTF_8);
        assertThat(printed, containsString("Выберите алгоритм:"));
        assertEquals(expected, choice);
    }

    @Test
    void getAlgorithmChoice_invalidThenValid_loopsUntilValid() throws IOException {
        String userInput = "-1\n3\n2\n";
        in = new ByteArrayInputStream(userInput.getBytes(StandardCharsets.UTF_8));
        ioAdapter = new StreamIOAdapter(out, in);
        menu = new Menu(ioAdapter);

        Menu.AlgorithmChoice choice = menu.getAlgorithmChoice();

        String printed = out.toString(StandardCharsets.UTF_8);
        assertThat(printed, containsString("Выберите алгоритм:"));
        assertThat(printed, containsString("Пожалуйста, введите число от 1 до 2."));
        assertEquals(Menu.AlgorithmChoice.RFL, choice);
    }

    static Stream<Arguments> outputChoiceProvider() {
        return Stream.of(
                Arguments.of("1\n", Menu.OutputChoice.CONSOLE),
                Arguments.of("2\n", Menu.OutputChoice.JSON_CONSOLE),
                Arguments.of("3\n", Menu.OutputChoice.JSON_FILE),
                Arguments.of("4\n", Menu.OutputChoice.RECOMMENDATIONS)
        );
    }

    @ParameterizedTest
    @MethodSource("outputChoiceProvider")
    void getOutputChoice_validInputs_selectCorrectEnum(String userInput, Menu.OutputChoice expected) throws IOException {
        in = new ByteArrayInputStream(userInput.getBytes(StandardCharsets.UTF_8));
        ioAdapter = new StreamIOAdapter(out, in);
        menu = new Menu(ioAdapter);

        Menu.OutputChoice choice = menu.getOutputChoice();

        String printed = out.toString(StandardCharsets.UTF_8);
        assertThat(printed, containsString("Выберите способ вывода данных:"));
        assertEquals(expected, choice);
    }

    @Test
    void getOutputChoice_invalidThenValid_loopsUntilValid() throws IOException {
        String userInput = "hello\n5\n3\n";
        in = new ByteArrayInputStream(userInput.getBytes(StandardCharsets.UTF_8));
        ioAdapter = new StreamIOAdapter(out, in);
        menu = new Menu(ioAdapter);

        Menu.OutputChoice choice = menu.getOutputChoice();

        String printed = out.toString(StandardCharsets.UTF_8);
        assertThat(printed, containsString("Выберите способ вывода данных:"));
        assertThat(printed, containsString("Некорректный ввод. Пожалуйста, введите число от 1 до 4."));
        assertThat(printed, containsString("Пожалуйста, введите число от 1 до 4."));
        assertEquals(Menu.OutputChoice.JSON_FILE, choice);
    }
}
