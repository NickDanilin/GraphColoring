package edu.spbstu.coloring;

import java.util.*;

public class RLFColoring implements ColoringAlgorithm {

    @Override
    public Map<ColoredVertex, int[]> colorGraph(List<int[]> graph) {
        int n = graph.size();
        if (n == 0) {
            return Collections.emptyMap();
        }

        // 1) Инициализируем color[v] = 0 для всех v=1..n
        int[] color = new int[n + 1]; // color[0] не используется

        int currentColor = 0;

        // Пока есть неокрашенные вершины
        while (true) {
            // 2a) U = множество всех неокрашенных:
            Set<Integer> U = new HashSet<>();
            for (int v = 1; v <= n; v++) {
                if (color[v] == 0) {
                    U.add(v);
                }
            }
            if (U.isEmpty()) {
                break; // все раскрашены
            }

            // 2b) Выбираем v0 ∈ U с максимальной исходной степенью (|neighbors(v0)|)
            int v0 = -1;
            int bestDegree = -1;
            for (int v : U) {
                int deg = graph.get(v - 1).length;
                if (deg > bestDegree) {
                    bestDegree = deg;
                    v0 = v;
                }
            }

            currentColor++;
            // 2c) Начинаем новый цветовой класс:
            Set<Integer> C = new HashSet<>();
            C.add(v0);

            // 2d) W = Н(v0) ∩ U (исключённые соседи)
            //     Затем удаляем из U: v0 и все элементы W
            Set<Integer> W = new HashSet<>();
            for (int u : graph.get(v0 - 1)) {
                if (U.contains(u)) {
                    W.add(u);
                }
            }
            U.remove(v0);
            U.removeAll(W);

            // 2e) Дополняем C по возможности:
            while (true) {
                // Сначала формируем кандидатов: {u ∈ U | для всех w ∈ C, (u не смежна с w)}
                List<Integer> candidates = getCandidates(graph, U, C);
                if (candidates.isEmpty()) {
                    break;
                }

                // Подсчитаем AW и AU для каждого кандидата
                int bestAW = -1;
                int bestAU = Integer.MAX_VALUE;
                int chosen = -1;
                for (int u : candidates) {
                    // AW[u] = |Н(u) ∩ W|
                    int awCount = 0;
                    for (int neighborOfU : graph.get(u - 1)) {
                        if (W.contains(neighborOfU)) {
                            awCount++;
                        }
                    }
                    // AU[u] = |Н(u) ∩ U|
                    int auCount = 0;
                    for (int neighborOfU : graph.get(u - 1)) {
                        if (U.contains(neighborOfU)) {
                            auCount++;
                        }
                    }
                    // Выбор u* (макс AW, при равенстве AW — мин AU)
                    if (awCount > bestAW || (awCount == bestAW && auCount < bestAU)) {
                        bestAW = awCount;
                        bestAU = auCount;
                        chosen = u;
                    }
                }

                // Добавляем chosen в C, удаляем из U
                C.add(chosen);
                U.remove(chosen);

                // Обновляем W и U:
                // newExcluded = Н(chosen) ∩ U
                Set<Integer> newExcluded = new HashSet<>();
                for (int neighborOfChosen : graph.get(chosen - 1)) {
                    if (U.contains(neighborOfChosen)) {
                        newExcluded.add(neighborOfChosen);
                    }
                }
                W.addAll(newExcluded);
                U.removeAll(newExcluded);
            }

            // 2f) Красим все вершины C в цвет currentColor
            for (int v : C) {
                color[v] = currentColor;
            }
        }

        // 3) Собираем возвращаемую карту Map<ColoredVertex,int[]>
        Map<ColoredVertex, int[]> result = new LinkedHashMap<>();
        for (int i = 1; i <= n; i++) {
            ColoredVertex cv = new ColoredVertex(i, color[i]);
            result.put(cv, graph.get(i - 1));
        }
        return result;
    }

    private static List<Integer> getCandidates(List<int[]> graph, Set<Integer> U, Set<Integer> C) {
        List<Integer> candidates = new ArrayList<>();
        for (int u : U) {
            boolean conflict = false;
            for (int w : C) {
                // если w сосед u
                for (int neighborOfW : graph.get(w - 1)) {
                    if (neighborOfW == u) {
                        conflict = true;
                        break;
                    }
                }
                if (conflict) break;
            }
            if (!conflict) {
                candidates.add(u);
            }
        }
        return candidates;
    }
}
