package edu.spbstu;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.spbstu.coloring.ColoredVertex;
import edu.spbstu.output.RecommendationUtility;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class RecommendationUtilityTest {

    @Test
    void getRecommendation_emptyGraph() {
        Map<ColoredVertex, int[]> emptyGraph = new LinkedHashMap<>();
        String[] recs = RecommendationUtility.getRecommendation(emptyGraph);

        // Ожидаем 4 строки:
        // 0: n=0, m=0
        // 1: maxColor=0
        // 2: плотность не определена
        // 3: рекомендация для n<=50
        assertThat(recs.length, is(4));
        assertThat(recs[0], is("Граф: n = 0 вершины, m = 0 рёбер."));
        assertThat(recs[1], is("Использовано цветов: 0."));
        assertThat(recs[2], is("Плотность графа не определена для n <= 1."));
        assertThat(recs[3], is("Рекомендация: для графов с n <= 50 можно попробовать RLF для компрессии (меньшего числа цветов)."));
    }

    @Test
    void getRecommendation_cycleOfFour_highDensity() {
        // Цикл 4 вершин: 1-2-3-4-1; раскраска в 2 цвета: {1,3}=1; {2,4}=2
        Map<ColoredVertex, int[]> graph = new LinkedHashMap<>();
        graph.put(new ColoredVertex(1, 1), new int[]{2, 4});
        graph.put(new ColoredVertex(2, 2), new int[]{1, 3});
        graph.put(new ColoredVertex(3, 1), new int[]{2, 4});
        graph.put(new ColoredVertex(4, 2), new int[]{1, 3});

        String[] recs = RecommendationUtility.getRecommendation(graph);

        // Вычислим вручную: n=4, totalNeighborRefs=8, m=4, density=8/(4*3)=0.666...
        // maxColor=2
        // Ожидаем 5 строк:
        // 0: "Граф: n = 4 вершины, m = 4 рёбер."
        // 1: "Использовано цветов: 2."
        // 2: "Граф плотный (плотность ≈ 0.667)."
        // 3: "Рекомендация: для графов с n <= 50..."
        // 4: "Поскольку граф плотный..."
        assertThat(recs.length, is(5));
        assertThat(recs[0], is("Граф: n = 4 вершины, m = 4 рёбер."));
        assertThat(recs[1], is("Использовано цветов: 2."));

        // Проверим, что строка о плотности содержит "Граф плотный" и число ~0.667
        assertThat(recs[2], startsWith("Граф плотный"));
        assertThat(recs[2], containsString("0,667"));

        assertThat(recs[3], is("Рекомендация: для графов с n <= 50 можно попробовать RLF для компрессии (меньшего числа цветов)."));
        assertThat(recs[4], is("Поскольку граф плотный, RLF может дать лучшее качество (использовать меньше цветов)."));
    }

    @Test
    void getRecommendation_largeSparseGraph() {
        // Построим цепной граф из 60 вершин: раскраска в 2 цвета (по четности)
        int n = 60;
        Map<ColoredVertex, int[]> graph = new LinkedHashMap<>();

        for (int i = 1; i <= n; i++) {
            int color = (i % 2 == 0) ? 2 : 1;
            if (i == 1) {
                graph.put(new ColoredVertex(1, color), new int[]{2});
            } else if (i == n) {
                graph.put(new ColoredVertex(n, color), new int[]{n - 1});
            } else {
                graph.put(new ColoredVertex(i, color), new int[]{i - 1, i + 1});
            }
        }

        String[] recs = RecommendationUtility.getRecommendation(graph);

        // Вычислим вручную:
        // totalNeighborRefs = 2*1 + 58*2 + 2*1 = 118, m = 59
        // density = 2*59 / (60*59) = 118/3540 ≈ 0.0333
        // maxColor = 2
        // Ожидаем 5 строк:
        // 0: "Граф: n = 60 вершины, m = 59 рёбер."
        // 1: "Использовано цветов: 2."
        // 2: "Граф разреженный (плотность ≈ 0.033)."
        // 3: "Рекомендация: для графов с n > 50..."
        // 4: "На очень разреженных и больших графах DSATUR обычно работает быстрее."
        assertThat(recs.length, is(5));
        assertThat(recs[0], is("Граф: n = 60 вершины, m = 59 рёбер."));
        assertThat(recs[1], is("Использовано цветов: 2."));

        // Плотность ~0.033
        assertThat(recs[2], startsWith("Граф разреженный"));
        assertThat(recs[2], containsString("0,033"));

        assertThat(recs[3], is("Рекомендация: для графов с n > 50 обычно быстрее и эффективнее использовать DSATUR."));
        assertThat(recs[4], is("На очень разреженных и больших графах DSATUR обычно работает быстрее."));
    }
}

