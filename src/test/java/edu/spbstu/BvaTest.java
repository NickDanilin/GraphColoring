package edu.spbstu;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.spbstu.menu.HelpInfoProvider;
import edu.spbstu.menu.IOAdapter;
import edu.spbstu.menu.StreamIOAdapter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BvaTest {

    private Path tempDir;
    private HelpInfoProvider helpInfoProvider;

    @BeforeAll
    void beforeAll() throws IOException {
        helpInfoProvider = Mockito.mock(HelpInfoProvider.class);
        tempDir = Files.createTempDirectory("bvaTemp");
    }

    @AfterAll
    void afterAll() throws IOException {
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    /**
     * TC-BVA-1: n=1, m=0, минимальный граф → 1 цвет, успех
     */
    @DisplayName("TC-BVA-1")
    @ParameterizedTest(name = "algo={0}")
    @ValueSource(strings = {"1","2"})
    void tcBva1_minimalGraph(String algo) throws IOException {
        // создаём JSON для n=1, m=0
        Path json = tempDir.resolve("bva1.json");
        Files.writeString(json,
                "{ \"adjacency_list\": { \"1\": [] } }",
                StandardCharsets.UTF_8
        );

        String input = Stream.of(
                "1",            // главное меню: загрузка данных
                "2",            // загрузка из файла
                json.toString(),
                "2",            // главное меню: выбор алгоритма
                algo,           // DSATUR/RFL
                "3",            // главное меню: получение результата
                "1",            // вывод в консоль
                "5"             // выход
        ).reduce((a,b)->a+"\n"+b).get()+"\n";

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOAdapter io = new StreamIOAdapter(bout, new ByteArrayInputStream(input.getBytes()));
        new App(io, helpInfoProvider).run();

        String out = bout.toString();
        assumeFalse(out.isEmpty());
        // единственная вершина должна получить цвет 1
        assertThat(out, containsString("Vertex 1: color = 1"));
    }

    /**
     * TC-BVA-2: n=0, m=0 (ниже минимума) → ошибка n
     */
    @DisplayName("TC-BVA-2")
    @ParameterizedTest(name = "algo={0}")
    @ValueSource(strings = {"1","2"})
    void tcBva2_belowMinimum(String algo) throws IOException {
        String input = Stream.of(
                "1",    // загрузка данных
                "1",    // ввод вручную
                "0\n",    // n=0
                "5"     // выход
        ).reduce((a,b)->a+"\n"+b).get()+"\n";

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOAdapter io = new StreamIOAdapter(bout, new ByteArrayInputStream(input.getBytes()));
        new App(io, helpInfoProvider).run();

        assertThat(bout.toString(), containsString("Количество вершин должно быть больше нуля"));
    }

    /**
     * TC-BVA-3: n=2, m=1, чуть выше минимума → двуцветная раскраска
     */
    @DisplayName("TC-BVA-3")
    @ParameterizedTest(name = "algo={0}")
    @ValueSource(strings = {"1","2"})
    void tcBva3_twoVertices(String algo) throws IOException {
        // JSON для n=2, edge 1-2
        Path json = tempDir.resolve("bva3.json");
        Files.writeString(json,
                "{ \"adjacency_list\": { \"1\":[2], \"2\":[1] } }",
                StandardCharsets.UTF_8
        );

        String input = Stream.of(
                "1","2",json.toString(),
                "2", algo,
                "3","1",
                "5"
        ).reduce((a,b)->a+"\n"+b).get()+"\n";

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOAdapter io = new StreamIOAdapter(bout, new ByteArrayInputStream(input.getBytes()));
        new App(io, helpInfoProvider).run();

        String out = bout.toString();
        assumeFalse(out.isEmpty());
        assertThat(out, containsString("color = 1"));
        assertThat(out, containsString("color = 2"));
        assertThat(out, not(containsString("color = 3")));
    }

    /**
     * TC-BVA-4: n=100, m=99, цепочка → успех раскраски (две краски)
     */
    @DisplayName("TC-BVA-4")
    @ParameterizedTest(name = "algo={0}")
    @ValueSource(strings = {"1","2"})
    void tcBva4_chain100(String algo) throws Exception {
        int n = 100;
        StringBuilder sb = new StringBuilder("{\"adjacency_list\":{");
        for (int i=1;i<=n;i++){
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
        Path json = tempDir.resolve("bva4.json");
        Files.writeString(json, sb.toString(), StandardCharsets.UTF_8);

        String input = Stream.of(
                "1","2",json.toString(),
                "2", algo,
                "3","1",
                "5"
        ).reduce((a,b)->a+"\n"+b).get()+"\n";

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOAdapter io = new StreamIOAdapter(bout, new ByteArrayInputStream(input.getBytes()));
        new App(io, helpInfoProvider).run();

        String out = bout.toString();
        assumeFalse(out.isEmpty());
        // только два цвета
        assertThat(out, containsString("color = 1"));
        assertThat(out, containsString("color = 2"));
        assertThat(out, not(containsString("color = 3")));
    }

    /**
     * TC-BVA-5: n=5, m=4 (минимальная связность) → проверка цепочки n-1
     */
    @DisplayName("TC-BVA-5")
    @ParameterizedTest(name = "algo={0}")
    @ValueSource(strings = {"1","2"})
    void tcBva5_chain5(String algo) throws IOException {
        // JSON для цепочки из 5
        String json = """
            {
              "adjacency_list": {
                "1":[2],
                "2":[1,3],
                "3":[2,4],
                "4":[3,5],
                "5":[4]
              }
            }
            """;
        Path f = tempDir.resolve("bva5.json");
        Files.writeString(f, json, StandardCharsets.UTF_8);

        String input = Stream.of(
                "1","2",f.toString(),
                "2", algo,
                "3","1",
                "5"
        ).reduce((a,b)->a+"\n"+b).get()+"\n";

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOAdapter io = new StreamIOAdapter(bout, new ByteArrayInputStream(input.getBytes()));
        new App(io, helpInfoProvider).run();

        String out = bout.toString();
        assumeFalse(out.isEmpty());
        assertThat(out, containsString("color = 1"));
        assertThat(out, containsString("color = 2"));
        assertThat(out, not(containsString("color = 3")));
    }

    /**
     * TC-BVA-6: n=5, m=3 (несвязный) → ошибка несвязности
     */
    @DisplayName("TC-BVA-6")
    @ParameterizedTest(name = "algo={0}")
    @ValueSource(strings = {"1","2"})
    void tcBva6_disconnected(String algo) throws IOException {
        String json = """
            { "adjacency_list": {
                "1":[2],
                "2":[1],
                "3":[],
                "4":[],
                "5":[]
            }}""";
        Path f = tempDir.resolve("bva6.json");
        Files.writeString(f, json, StandardCharsets.UTF_8);

        String input = Stream.of(
                "1","2",f.toString(),
                "5"
        ).reduce((a,b)->a+"\n"+b).get()+"\n";

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOAdapter io = new StreamIOAdapter(bout, new ByteArrayInputStream(input.getBytes()));
        new App(io, helpInfoProvider).run();

        assertThat(bout.toString(), containsString("not connected"));
    }

    /**
     * TC-BVA-7: n=5, m=10 (полный граф K5) → 5 цветов
     */
    @DisplayName("TC-BVA-7")
    @ParameterizedTest(name = "algo={0}")
    @ValueSource(strings = {"1","2"})
    void tcBva7_completeK5(String algo) throws IOException {
        StringBuilder sb = new StringBuilder("{\"adjacency_list\":{");
        for(int i=1;i<=5;i++){
            sb.append("\"").append(i).append("\":[");
            for(int j=1;j<=5;j++){
                if(j!=i) sb.append(j).append(",");
            }
            sb.setLength(sb.length()-1); // убрать запятую
            sb.append("]");
            if(i<5) sb.append(",");
        }
        sb.append("}}");
        Path f = tempDir.resolve("bva7.json");
        Files.writeString(f, sb.toString(), StandardCharsets.UTF_8);

        String input = Stream.of(
                "1","2",f.toString(),
                "2", algo,
                "3","1",
                "5"
        ).reduce((a,b)->a+"\n"+b).get()+"\n";

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOAdapter io = new StreamIOAdapter(bout, new ByteArrayInputStream(input.getBytes()));
        new App(io, helpInfoProvider).run();

        String out = bout.toString();
        assumeFalse(out.isEmpty());
        // проверяем, что найдены цвета 1..5 и нет 6
        for(int c=1;c<=5;c++){
            assertThat(out, containsString("color = " + c));
        }
        assertThat(out, not(containsString("color = 6")));
    }

    /**
     * TC-BVA-8: n=5, m>max (ошибка m) → сосед вне диапазона
     */
    @DisplayName("TC-BVA-8")
    @ParameterizedTest(name = "algo={0}")
    @ValueSource(strings = {"1","2"})
    void tcBva8_excessEdges(String algo) throws IOException {
        String json = """
            { "adjacency_list": {
                "1":[2,3,4,5,6],
                "2":[1],
                "3":[1],
                "4":[1],
                "5":[1]
            }}""";
        Path f = tempDir.resolve("bva8.json");
        Files.writeString(f, json, StandardCharsets.UTF_8);

        String input = Stream.of("1","2",f.toString(),"5")
                               .reduce((a,b)->a+"\n"+b).get()+"\n";
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOAdapter io = new StreamIOAdapter(bout, new ByteArrayInputStream(input.getBytes()));
        new App(io, helpInfoProvider).run();

        assertThat(bout.toString(), containsString("out of range"));
    }
}
