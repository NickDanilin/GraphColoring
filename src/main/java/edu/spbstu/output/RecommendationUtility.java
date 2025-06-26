package edu.spbstu.output;

import edu.spbstu.coloring.ColoredVertex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecommendationUtility {
    private RecommendationUtility() {
    }

    public static String[] getRecommendation(Map<ColoredVertex, int[]> graph) {
        int n = graph.size();
        int totalNeighborRefs = 0;
        int maxColor = 0;

        //totalNeighborRefs и максимальный цвет
        for (Map.Entry<ColoredVertex, int[]> entry : graph.entrySet()) {
            ColoredVertex cv = entry.getKey();
            maxColor = Math.max(maxColor, cv.getColor());
            int[] neighbors = entry.getValue();
            if (neighbors != null) {
                totalNeighborRefs += neighbors.length;
            }
        }
        // Поскольку каждая пара (i,j) фигурирует в списке соседей дважды (i->j и j->i),
        // число рёбер m = totalNeighborRefs / 2
        int m = totalNeighborRefs / 2;

        // Плотность графа p = 2m / (n (n - 1))
        double density = 0.0;
        if (n > 1) {
            density = (2.0 * m) / (n * (n - 1));
        }

        List<String> recs = new ArrayList<>();

        // 1. Информация о размере графа и числе рёбер
        recs.add(String.format("Граф: n = %d вершины, m = %d рёбер.", n, m));

        // 2. Информация о числе использованных цветов
        recs.add(String.format("Использовано цветов: %d.", maxColor));

        // 3. Оценка плотности
        String densityDesc;
        if (n <= 1) {
            densityDesc = "Плотность графа не определена для n <= 1.";
        } else if (density < 0.1) {
            densityDesc = String.format("Граф разреженный (плотность ≈ %.3f).", density);
        } else if (density < 0.5) {
            densityDesc = String.format("Граф средней плотности (плотность ≈ %.3f).", density);
        } else {
            densityDesc = String.format("Граф плотный (плотность ≈ %.3f).", density);
        }
        recs.add(densityDesc);

        // 4. Рекомендации по выбору алгоритма
        if (n > 50) {
            recs.add("Рекомендация: для графов с n > 50 обычно быстрее и эффективнее использовать DSATUR.");
        } else {
            recs.add("Рекомендация: для графов с n <= 50 можно попробовать RLF для компрессии (меньшего числа цветов).");
        }
        if (density > 0.5) {
            recs.add("Поскольку граф плотный, RLF может дать лучшее качество (использовать меньше цветов).");
        } else if (density < 0.1 && n > 50) {
            recs.add("На очень разреженных и больших графах DSATUR обычно работает быстрее.");
        }
        return recs.toArray(new String[0]);
    }
}
