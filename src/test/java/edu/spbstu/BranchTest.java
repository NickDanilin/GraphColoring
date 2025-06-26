package edu.spbstu;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.spbstu.menu.HelpInfoProvider;
import edu.spbstu.menu.IOAdapter;
import edu.spbstu.menu.StreamIOAdapter;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BranchTest {

    private HelpInfoProvider helpInfoProvider;
    private Path tempDir;

    @BeforeAll
    void setUpAll() throws IOException {
        helpInfoProvider = mock(HelpInfoProvider.class);
        tempDir = Files.createTempDirectory("branchTemp");
    }

    @AfterAll
    void tearDownAll() throws IOException {
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    /**
     * TC-BR-1:
     * выбор File → загрузка валидного JSON →
     * выбор DSATUR → консольный вывод → выход
     * покрытие ветвей B-BR1, B-BR3(true→format OK), B-BR4(true→connected), B-BR5, B-BR8
     */
    @Test
    @DisplayName("TC-BR-1")
    void tcBr1_fileValidDsaturConsole() throws Exception {
        // JSON с графом, вызывающим DSATUR tie-break внутри
        Path json = tempDir.resolve("br1.json");
        Files.writeString(json,
                """
                        {
                          "adjacency_list": {
                            "1":[2,3],
                            "2":[1,4],
                            "3":[1],
                            "4":[2]
                          }
                        }
                        """, StandardCharsets.UTF_8);

        String input = Stream.of(
                "1",                // главное меню: загрузка данных
                "2",                // InputChoice: файл
                json.toString(),    // путь
                "2",                // меню: выбор алгоритма
                "1",                // AlgorithmChoice: DSATUR
                "3",                // меню: получение результата
                "1",                // OutputChoice: консоль
                "5"                 // выход
        ).reduce((a, b) -> a + "\n" + b).get() + "\n";

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOAdapter io = new StreamIOAdapter(bout, new ByteArrayInputStream(input.getBytes()));
        new App(io, helpInfoProvider).run();

        String out = bout.toString();
        assumeFalse(out.isEmpty());
        // Убедимся, что DSATUR запустился и отобразил хотя бы одну строку раскраски
        assertThat(out, containsString("Vertex 1:"));
        assertThat(out, containsString("color = "));
    }

    /**
     * TC-BR-2:
     * выбор Console → ручной ввод валидных данных →
     * выбор RLF → saveToFile
     * покрытие ветвей B-BR2, B-BR3(format OK), B-BR4(connected), B-BR6, B-BR7
     */
    @Test
    @DisplayName("TC-BR-2")
    void tcBr2_manualValidRlfSaveToFile() throws Exception {
        // Консольный ввод цепочки n=4
        String manual =
                "4\n" +    // n
                "2\n" +    // neighbors of 1
                "1 3\n" +  // neighbors of 2
                "2 4\n" +  // neighbors of 3
                "3\n";     // neighbors of 4

        Path outFile = tempDir.resolve("br2_result.json");

        String input = Stream.of(
                "1",                // главное меню: загрузка данных
                "1",                // InputChoice: ручной ввод
                manual.trim(),
                "2",                // выбор алгоритма
                "2",                // AlgorithmChoice: RFL
                "3",                // получение результата
                "3",                // OutputChoice: JSON файл
                outFile.toString(),
                "5"                 // выход
        ).reduce((a, b) -> a + "\n" + b).get() + "\n";

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOAdapter io = new StreamIOAdapter(bout, new ByteArrayInputStream(input.getBytes()));
        new App(io, helpInfoProvider).run();

        // Проверяем, что файл создан и содержит JSON
        assertTrue(Files.exists(outFile));
        String json = Files.readString(outFile);
        assertThat(json, Matchers.startsWith("{"));
        assertThat(json, containsString("\"color\""));
    }

    /**
     * TC-BR-3:
     * попытка DSATUR без загрузки → некорректный формат JSON текст →
     * выбор Console с ошибкой формата
     * покрытие ветвей B-BR5 без загрузки, B-BR3(false→format error), B-BR2
     */
    @Test
    @DisplayName("TC-BR-3")
    void tcBr3_dsaturWithoutLoad_thenConsoleFormatError() throws Exception {
        String badJsonInput = "{ not json }";

        String input = Stream.of(
                "2",                // главное меню: выбор алгоритма сразу
                "3",                // получение результата
                "1",                // назад: загрузка данных
                "3",                // InputChoice: ввод JSON
                badJsonInput,       // ввод текста JSON
                // этот JSON ошибочный
                "1",                // возврат
                "1",                // InputChoice: ручной ввод → проверка формата
                "3",                // n
                "a b",              // неверный ввод
                "1 3",              // верный ввод
                "2",                // верный ввод
                "5"
        ).reduce((a, b) -> a + "\n" + b).get() + "\n";

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOAdapter io = new StreamIOAdapter(bout, new ByteArrayInputStream(input.getBytes()));
        new App(io, helpInfoProvider).run();

        String out = bout.toString();
        // Должны увидеть сообщение об ошибке формата
        assertThat(out, containsString("Сначала загрузите данные"));
        assertThat(out, containsString("Сначала выполните алгоритм"));
        assertThat(out, containsString("Ошибка валидации"));
        assertThat(out, containsString("Input must contain only numbers and spaces"));
    }
}
