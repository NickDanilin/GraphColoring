package edu.spbstu;


import com.fasterxml.jackson.databind.ObjectMapper;
import edu.spbstu.menu.HelpInfoProvider;
import edu.spbstu.menu.IOAdapter;
import edu.spbstu.menu.StreamIOAdapter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EPTest {

    private HelpInfoProvider helpInfoProvider;
    private Path tempDir;

    @BeforeAll
    void beforeAll() throws IOException {
        helpInfoProvider = mock(HelpInfoProvider.class);
        tempDir = Files.createTempDirectory("tempDir");
    }

    @AfterAll
    void afterAll() throws IOException {
        // чистим временный каталог
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    /**
     * TC-EP-1: ручной ввод, Small, Bipartite, вывод в консоль
     */
    @DisplayName("TC-EP-1")
    @ParameterizedTest(name = "algo={0}")
    @ValueSource(strings = {"1","2"})
    void tcEp1(String algoChoice) throws IOException {
        String input = Stream.of(
                "1",      // главное меню: загрузка данных
                "1",      // ввод вручную
                "3",      // n=3 (Small)
                "2",      // соседи 1
                "1 3",    // соседи 2
                "2",      // соседи 3
                "2",      // главное меню: выбор алгоритма
                algoChoice,// 1=DSATUR,2=RFL
                "3",      // главное меню: получение результата
                "1",      // вывод в консоль
                "5"       // выход
        ).reduce((a,b)->a+"\n"+b).get() + "\n";

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayInputStream bin = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        IOAdapter io = new StreamIOAdapter(bout, bin);

        new App(io, helpInfoProvider).run();

        String out = bout.toString(StandardCharsets.UTF_8);
        assumeFalse(out.isEmpty());
        // для Bipartite χ=2, проверяем отсутствие цвета 3
        assertThat(out, containsString("color = 1"));
        assertThat(out, containsString("color = 2"));
        assertThat(out, not(containsString("color = 3")));
    }

    /**
     * TC-EP-2: файл, Medium, NonBipartite, вывод в консоль
     */
    @DisplayName("TC-EP-2")
    @ParameterizedTest(name = "algo={0}")
    @ValueSource(strings = {"1","2"})
    void tcEp2(String algoChoice) throws Exception {
        // создаём JSON-файл с n=6, m=7, NonBipartite
        Path json = tempDir.resolve("ep2.json");
        String content = """
            { "adjacency_list": {
              "1":[2,3],
              "2":[1,3,4],
              "3":[1,2,5],
              "4":[2,5,6],
              "5":[3,4,6],
              "6":[4,5]
            }}""";
        Files.writeString(json, content, StandardCharsets.UTF_8);

        String input = Stream.of(
                "1","2",json.toString(),
                "2", algoChoice,
                "3","1","5"   // result→console, exit
        ).reduce((a,b)->a+"\n"+b).get()+"\n";

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayInputStream bin = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        IOAdapter io = new StreamIOAdapter(bout, bin);

        new App(io, helpInfoProvider).run();

        String out = bout.toString(StandardCharsets.UTF_8);
        assumeFalse(out.isEmpty());
        // NonBipartite ⇒ минимум 3 цвета
        assertThat(out, anyOf(containsString("color = 3"), containsString("color = 4")));
    }

    /**
     * TC-EP-3: файл, Large, Bipartite, вывод в JSON-файл
     */
    @DisplayName("TC-EP-3")
    @ParameterizedTest(name = "algo={0}")
    @ValueSource(strings = {"1","2"})
    void tcEp3(String algoChoice) throws Exception {
        // создаём цепочку из 51 вершины (бипартитный)
        int n = 51;
        StringBuilder sb = new StringBuilder("{\"adjacency_list\":{");
        for(int i=1;i<=n;i++){
            sb.append("\"").append(i).append("\":[");
            if(i>1) sb.append(i-1);
            if(i<n){
                if(i>1) sb.append(",");
                sb.append(i+1);
            }
            sb.append("]");
            if(i<n) sb.append(",");
        }
        sb.append("}}");
        Path json = tempDir.resolve("ep3.json");
        Files.writeString(json, sb.toString(), StandardCharsets.UTF_8);

        Path outFile = tempDir.resolve("ep3_out.json");
        String input = Stream.of(
                "1","2",json.toString(),
                "2", algoChoice,
                "3","3",outFile.toString(),
                "5"
        ).reduce((a,b)->a+"\n"+b).get()+"\n";

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayInputStream bin = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        IOAdapter io = new StreamIOAdapter(bout, bin);

        new App(io, helpInfoProvider).run();

        // проверяем, что файл создан, и в нём только 2 цвета
        var root = new ObjectMapper().readTree(Files.readString(outFile));
        root.fieldNames().forEachRemaining(key -> {
            int c = root.get(key).get("color").asInt();
            assertThat(c, anyOf(is(1), is(2)));
        });
    }

    /** TC-EP-4: Invalid-JSON-Syntax */
    @DisplayName("TC-EP-4")
    @ParameterizedTest(name = "algo={0}")
    @ValueSource(strings = {"1","2"})
    void tcEp4(String ignored) throws IOException {
        Path bad = tempDir.resolve("ep4.json");
        Files.writeString(bad, "{ adjacency_list: [1,}", StandardCharsets.UTF_8);

        String input = Stream.of("1","2",bad.toString(),"5")
                               .reduce((a,b)->a+"\n"+b).get()+"\n";
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayInputStream bin = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        IOAdapter io = new StreamIOAdapter(bout, bin);

        new App(io, helpInfoProvider).run();

        assertThat(bout.toString(), containsString("Failed to parse JSON"));
    }

    /** TC-EP-5: Invalid-Missing-Field */
    @DisplayName("TC-EP-5")
    @ParameterizedTest(name = "algo={0}")
    @ValueSource(strings = {"1","2"})
    void tcEp5(String ignored) throws IOException {
        Path f = tempDir.resolve("ep5.json");
        Files.writeString(f, "{ \"wrong\":{} }", StandardCharsets.UTF_8);

        String input = Stream.of("1","2",f.toString(),"5")
                               .reduce((a,b)->a+"\n"+b).get()+"\n";
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayInputStream bin = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        IOAdapter io = new StreamIOAdapter(bout, bin);

        new App(io, helpInfoProvider).run();

        assertThat(bout.toString(), containsString("non-empty 'adjecency_list'"));
    }

    /** TC-EP-6: Invalid-Non-Symmetric */
    @DisplayName("TC-EP-6")
    @ParameterizedTest(name = "algo={0}")
    @ValueSource(strings = {"1","2"})
    void tcEp6(String ignored) throws IOException {
        Path f = tempDir.resolve("ep6.json");
        Files.writeString(f, "{\"adjacency_list\":{\"1\":[2],\"2\":[]}}", StandardCharsets.UTF_8);

        String input = Stream.of("1","2",f.toString(),"5")
                               .reduce((a,b)->a+"\n"+b).get()+"\n";
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayInputStream bin = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        IOAdapter io = new StreamIOAdapter(bout, bin);

        new App(io, helpInfoProvider).run();

        assertThat(bout.toString(), containsString("not symmetric"));
    }

    /** TC-EP-7: Invalid-Console-NonNumeric */
    @DisplayName("TC-EP-7")
    @ParameterizedTest(name = "algo={0}")
    @ValueSource(strings = {"1","2"})
    void tcEp7(String ignored) throws IOException {
        String input = Stream.of(
                "1","1","3","2 A","1 3","2","5"
        ).reduce((a,b)->a+"\n"+b).get()+"\n";
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayInputStream bin = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        IOAdapter io = new StreamIOAdapter(bout, bin);

        new App(io, helpInfoProvider).run();

        assertThat(bout.toString(), containsString("only numbers and spaces"));
    }

    /** TC-EP-8: Invalid-Console-OutOfRange */
    @DisplayName("TC-EP-8")
    @ParameterizedTest(name = "algo={0}")
    @ValueSource(strings = {"1","2"})
    void tcEp8(String ignored) throws IOException {
        String input = Stream.of(
                "1","1","3","2 4","1 3","2","5"
        ).reduce((a,b)->a+"\n"+b).get()+"\n";
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayInputStream bin = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        IOAdapter io = new StreamIOAdapter(bout, bin);

        new App(io, helpInfoProvider).run();

        assertThat(bout.toString(), containsString("between 1 and 3"));
    }

    /** TC-EP-9: Invalid-Console-DuplicateEdges */
    @DisplayName("TC-EP-9")
    @ParameterizedTest(name = "algo={0}")
    @ValueSource(strings = {"1","2"})
    void tcEp9(String ignored) throws IOException {
        String input = Stream.of(
                "1","1","3","2 2 3","1 3","2","5"
        ).reduce((a,b)->a+"\n"+b).get()+"\n";
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayInputStream bin = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        IOAdapter io = new StreamIOAdapter(bout, bin);

        new App(io, helpInfoProvider).run();

        assertThat(bout.toString(), containsString("Duplicate neighbor"));
    }

    /** TC-EP-10: manual, Disconnected */
    @DisplayName("TC-EP-10")
    @ParameterizedTest(name = "algo={0}")
    @ValueSource(strings = {"1","2"})
    void tcEp10(String ignored) throws IOException {
        String input = Stream.of(
                "1","1","4",
                "2","1","","",
                "5"
        ).reduce((a,b)->a+"\n"+b).get()+"\n";
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayInputStream bin = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        IOAdapter io = new StreamIOAdapter(bout, bin);

        new App(io, helpInfoProvider).run();

        assertThat(bout.toString(), containsString("Input cannot be null or empty"));
    }
}

