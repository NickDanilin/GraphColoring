package edu.spbstu.coloring;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DSaturColoring implements ColoringAlgorithm {

    @Override
    public Map<ColoredVertex, int[]> colorGraph(List<int[]> graph) {
        int n = graph.size();  // число вершин (мы предполагаем, что graph.size() = n)
        if (n == 0) {
            // Пустой граф – нечего красить. Вернём пустую Map.
            return Collections.emptyMap();
        }

        // --- 1) Построим вспомогательные структуры ---
        // color[v] (индексируем с 1 до n), поэтому создаём массив длины n+1
        int[] color = new int[n + 1];         // color[0] не используется
        int[] saturation = new int[n + 1];    // saturation[0] не используется
        int[] degree = new int[n + 1];        // degree[v] = число соседей v

        // Заполним degree и инициализируем color=0, saturation=0
        for (int i = 1; i <= n; i++) {
            color[i] = 0;
            saturation[i] = 0;
            int[] neigh = graph.get(i - 1);
            degree[i] = neigh.length;
        }

        // Установим множество неокрашенных вершин:
        Set<Integer> uncolored = new HashSet<>();
        for (int v = 1; v <= n; v++) {
            uncolored.add(v);
        }

        // --- 2) Выбор первой вершины: с максимальной degree ---
        int first = 1;
        for (int v = 2; v <= n; v++) {
            if (degree[v] > degree[first]) {
                first = v;
            }
        }
        color[first] = 1;
        uncolored.remove(first);

        // --- 3) Обновляем saturation соседей first ---
        for (int u : graph.get(first - 1)) {
            if (uncolored.contains(u)) {
                saturation[u]++;
            }
        }

        // --- 4) Основной цикл DSATUR ---
        while (!uncolored.isEmpty()) {
            // 4a) Найдём вершину v* с максимальным (saturation, degree)
            int best = -1;
            for (int v : uncolored) {
                if (best < 0
                    || saturation[v] > saturation[best]
                    || (saturation[v] == saturation[best] && degree[v] > degree[best])) {
                    best = v;
                }
            }
            int vstar = best;

            // 4b) Собираем все запрещённые цвета:
            Set<Integer> forbidden = new HashSet<>();
            for (int u : graph.get(vstar - 1)) {
                if (color[u] != 0) {
                    forbidden.add(color[u]);
                }
            }

            // 4c) Ищем минимальный c ≥ 1, не входящий в forbidden
            int c = 1;
            while (forbidden.contains(c)) {
                c++;
            }

            // 4d) Красим vstar в найденный цвет c
            color[vstar] = c;
            uncolored.remove(vstar);

            // 4e) Обновляем saturation соседей vstar, которые ещё неокрашены:
            for (int u : graph.get(vstar - 1)) {
                if (uncolored.contains(u)) {
                    // Пересчитаем saturation[u] "с нуля":
                    // (число уникальных цветов среди соседей u)
                    Set<Integer> neighborColors = new HashSet<>();
                    for (int w : graph.get(u - 1)) {
                        if (color[w] != 0) {
                            neighborColors.add(color[w]);
                        }
                    }
                    saturation[u] = neighborColors.size();
                }
            }
        }

        // --- 5) Собираем возвращаемую Map<ColoredVertex, int[]> ---
        Map<ColoredVertex, int[]> result = new LinkedHashMap<>();
        for (int i = 1; i <= n; i++) {
            ColoredVertex cv = new ColoredVertex(i, color[i]);
            // В качестве значения кладём оригинальный массив соседей:
            int[] originalNeighbors = graph.get(i - 1);
            result.put(cv, originalNeighbors);
        }

        return result;
    }
}