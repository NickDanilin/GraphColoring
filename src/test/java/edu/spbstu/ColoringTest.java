package edu.spbstu;

import edu.spbstu.coloring.ColoredVertex;
import edu.spbstu.coloring.DSaturColoring;
import edu.spbstu.coloring.RLFColoring;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ColoringTest {
    /**
     * Тест 1: оба алгоритма (DSatur и RLF) должны дать идентичную раскраску
     * для треугольного графа (3 вершины, каждый связан с каждым).
     */
    @Test
    void bothAlgorithmsProduceSameColoringOnTriangle() {
        // Треугольник: 1↔2, 2↔3, 3↔1
        List<int[]> triangle = List.of(
                new int[]{2, 3},  // соседи вершины 1
                new int[]{1, 3},  // соседи вершины 2
                new int[]{1, 2}   // соседи вершины 3
        );

        DSaturColoring dsatur = new DSaturColoring();
        RLFColoring rlf = new RLFColoring();

        Map<ColoredVertex, int[]> dsaturResult = dsatur.colorGraph(triangle);
        Map<ColoredVertex, int[]> rlfResult = rlf.colorGraph(triangle);

        // Преобразуем результаты в Map<vertexId, color>
        Map<Integer, Integer> dsMap = new HashMap<>();
        for (ColoredVertex cv : dsaturResult.keySet()) {
            dsMap.put(cv.getVertexId(), cv.getColor());
        }

        Map<Integer, Integer> rlfMap = new HashMap<>();
        for (ColoredVertex cv : rlfResult.keySet()) {
            rlfMap.put(cv.getVertexId(), cv.getColor());
        }

        // Убедимся, что оба алгоритма раскрасили ровно 3 вершины
        assertThat(dsMap.keySet(), containsInAnyOrder(1, 2, 3));
        assertThat(rlfMap.keySet(), containsInAnyOrder(1, 2, 3));

        // И что цвет каждой вершины совпадает
        for (int vertex = 1; vertex <= 3; vertex++) {
            int colorDs = dsMap.get(vertex);
            int colorRlf = rlfMap.get(vertex);
            assertEquals(colorDs, colorRlf,
                    "Vertex " + vertex + " should have same color in both algorithms");
        }

        // Итого: раскраска треугольника требует 3 цвета
        Set<Integer> usedColors = new HashSet<>(dsMap.values());
        assertThat(usedColors.size(), Matchers.is(3));
    }

    /**
     * Тест 2: проверка корректности раскраски DSatur на циклическом графе из 4 вершин.
     * Ожидаемый χ(C4) = 2. Убедимся, что соседние вершины имеют разные цвета.
     */
    @Test
    void dsaturProducesValidColoringOnCycleOfFour() {
        // Цикл из 4 вершин: 1-2-3-4-1
        List<int[]> cycle4 = List.of(
                new int[]{2, 4},  // соседи вершины 1
                new int[]{1, 3},  // соседи вершины 2
                new int[]{2, 4},  // соседи вершины 3
                new int[]{1, 3}   // соседи вершины 4
        );

        DSaturColoring dsatur = new DSaturColoring();
        Map<ColoredVertex, int[]> result = dsatur.colorGraph(cycle4);

        // Преобразуем в Map<vertexId, color>
        Map<Integer, Integer> colorMap = new HashMap<>();
        for (ColoredVertex cv : result.keySet()) {
            colorMap.put(cv.getVertexId(), cv.getColor());
        }

        // Убедимся, что раскрашено именно 4 вершины
        assertThat(colorMap.keySet(), containsInAnyOrder(1, 2, 3, 4));

        // Для каждой вершины проверим, что её цвет отличается от цветов всех соседей
        for (ColoredVertex cv : result.keySet()) {
            int vid = cv.getVertexId();
            int vcolor = cv.getColor();
            int[] neighbors = result.get(cv);
            for (int neigh : neighbors) {
                int neighborColor = colorMap.get(neigh);
                assertNotEquals(vcolor, neighborColor,
                        String.format("Vertex %d and its neighbor %d have same color %d", vid, neigh, vcolor));
            }
        }

        // Убедимся, что использовано ровно 2 цвета (граф двудольный)
        Set<Integer> usedColors = new HashSet<>(colorMap.values());
        assertThat(usedColors.size(), Matchers.is(2));
    }
}
