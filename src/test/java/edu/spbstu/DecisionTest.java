package edu.spbstu;

import edu.spbstu.menu.IOAdapter;
import edu.spbstu.menu.StreamIOAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.spbstu.input.InputConverterUtility;
import edu.spbstu.input.InputValidationException;
import edu.spbstu.coloring.DSaturColoring;
import edu.spbstu.coloring.RLFColoring;
import edu.spbstu.coloring.ColoredVertex;

class DecisionTest {

    // D-DT1: validFormat=false, connected=true → форматная ошибка
    @Test
    @DisplayName("TC-DT-1")
    void tcDt1_formatFalseConnectedTrue() {
        // Нелетальный JSON-ввод, чтобы validFormat=false, connected=true не срабатывала
        String badJson = "{ adjacency_list: [1,2] }";

        // вызов convertFromJson бросает InputValidationException
        InputValidationException ex = assertThrows(InputValidationException.class, () ->
                InputConverterUtility.convertFromJson(badJson)
        );
        assertThat(ex.getMessage(), containsString("Failed to parse JSON"));
    }

    // D-DT1: validFormat=true, connected=false → ошибка несвязности
    @Test
    @DisplayName("TC-DT-2")
    void tcDt2_formatTrueConnectedFalse() throws InputValidationException {
        // Корректный синтаксис, симметричность, но несвязный граф из 2 компонент
        String json = """
            {
              "adjacency_list": {
                "1": [2],
                "2": [1],
                "3": []
              }
            }
            """;
        // validate выбросит несвязность
        InputValidationException ex = assertThrows(InputValidationException.class, () ->
                InputConverterUtility.convertFromJson(json)
        );
        assertThat(ex.getMessage(), containsString("not connected"));
    }

    // D-DT1: validFormat=true, connected=true → продолжаем
    @Test
    @DisplayName("TC-DT-3")
    void tcDt3_formatTrueConnectedTrue() throws InputValidationException {
        String json = """
            {
              "adjacency_list": {
                "1": [2],
                "2": [1]
              }
            }
            """;
        // должно успешно парситься и валидироваться
        List<int[]> graph = InputConverterUtility.convertFromJson(json);
        // не должно бросать
        InputConverterUtility.validate(graph);
        // и даже алгоритмы должны отработать
        var ds = new DSaturColoring().colorGraph(graph);
        var rlf = new RLFColoring().colorGraph(graph);
        // оба должны раскрасить две вершины
        assertThat(ds.keySet(), hasSize(2));
        assertThat(rlf.keySet(), hasSize(2));
    }

    // D-DT2: choice != DSATUR & != RLF → getAlgorithmChoice циклично спрашивает
    @Test
    @DisplayName("TC-DT-4")
    void tcDt4_choiceOtherLoopsToValid() throws Exception {
        String input = "5\n0\n2\n";  // две ошибки, затем корректный «2»
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOAdapter io = new StreamIOAdapter(bout,
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))
        );
        // вызываем только getAlgorithmChoice
        var menu = new edu.spbstu.menu.Menu(io);
        var choice = menu.getAlgorithmChoice();
        assertThat(choice, is(edu.spbstu.menu.Menu.AlgorithmChoice.RFL));
        String output = bout.toString();
        assertThat(output, containsString("Пожалуйста, введите число от 1 до 2"));
    }

    // D-DT2: choice == RLF → ветвь RLF
    @Test
    @DisplayName("TC-DT-5")
    void tcDt5_choiceRLF() throws Exception {
        String input = "2\n";
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOAdapter io = new StreamIOAdapter(bout,
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))
        );
        var menu = new edu.spbstu.menu.Menu(io);
        var choice = menu.getAlgorithmChoice();
        assertThat(choice, is(edu.spbstu.menu.Menu.AlgorithmChoice.RFL));
        // ни одного сообщения об ошибке
        assertThat(bout.toString(), not(containsString("Некорректный")));
    }

    // D-DT3/D-DT4: сатурация tie-break в DSATUR (sat==maxSat && deg>maxDeg)
    @Test
    @DisplayName("TC-DT-6")
    void tcDt6_dsaturTieBreak() {
        // Граф: 3–1,3–2,3–4,2–4  (см. анализ)
        List<int[]> graph = List.of(
                new int[]{3},       // 1–3
                new int[]{3,4},     // 2–3,2–4
                new int[]{1,2,4},   // 3–1,3–2,3–4
                new int[]{2,3}      // 4–2,4–3
        );
        var result = new DSaturColoring().colorGraph(graph);
        // vertex 3 всегда первым, vertex 2 — tie-break выбор => color2 == 2
        int color2 = result.entrySet().stream()
                .filter(e -> e.getKey().getVertexId()==2)
                .findFirst().get().getKey().getColor();
        assertThat(color2, is(2));
    }

    // D-DT5: AW/AU tie-break в RLF (AW==bestAW && AU<bestAU)
    @Test
    @DisplayName("TC-DT-7")
    void tcDt7_rlfTieBreak() {
        // Построим граф: {1,2,3,4}, edges: (1–3),(1–4),(2–3),(2–4)
        // K_{2,2}: U={1,2}, V={3,4}. Это двудольный.
        List<int[]> graph = List.of(
                new int[]{3,4},     // 1
                new int[]{3,4},     // 2
                new int[]{1,2},     // 3
                new int[]{1,2}      // 4
        );
        var result = new RLFColoring().colorGraph(graph);
        // В K_{2,2} RLF должен собрать сначала максимально независимый класс {1,2}
        // и покрасить их в color=1. Проверим, что color1 == color2 == 1, а 3,4 — >2
        Map<Integer,Integer> cmap = result.keySet().stream()
                .collect(java.util.stream.Collectors.toMap(ColoredVertex::getVertexId, ColoredVertex::getColor));
        assertThat(cmap.get(1), is(1));
        assertThat(cmap.get(2), is(1));
        assertThat(cmap.get(3), is(2));
        assertThat(cmap.get(4), is(2));
    }
}

