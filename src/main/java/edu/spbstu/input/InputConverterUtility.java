package edu.spbstu.input;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

public class InputConverterUtility {
    private InputConverterUtility() {
    }

    public static List<int[]> convertFromUserInput(List<String> inputs, int verticesCount)
            throws InputValidationException {

        if (inputs == null || inputs.isEmpty()) {
            throw new InputValidationException("Input list cannot be null or empty");
        }

        if (inputs.size() != verticesCount) {
            throw new InputValidationException("Input list size must be equal to the number of vertices: "
                                               + verticesCount);
        }

        for (String input : inputs) {
            if (input == null || input.isEmpty()) {
                throw new InputValidationException("Input cannot be null or empty");
            }
        }


        for (String input : inputs) {
            if (!input.matches("[\\d\\s]+")) {
                throw new InputValidationException("Input must contain only numbers and spaces: " + input);
            }
        }

        var res = inputs.stream()
                .map(input -> {
                    String[] parts = input.trim().split("\\s+");
                    return Arrays.stream(parts)
                            .mapToInt(Integer::parseInt)
                            .toArray();
                })
                .toList();
        if (res.stream().flatMapToInt(Arrays::stream).anyMatch(i -> i < 1 || i > verticesCount)) {
            throw new InputValidationException("Input values must be between 1 and " + verticesCount);
        }

        validate(res);
        return res;
    }

    public static List<int[]> convertFromJson(String json) throws InputValidationException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Map<String, List<Integer>>> parsedJson = objectMapper.readValue(json, Map.class);

            Map<String, List<Integer>> adjacencyMap = parsedJson.get("adjacency_list");
            if (adjacencyMap == null || adjacencyMap.isEmpty()) {
                throw new InputValidationException("JSON must contain a non-empty 'adjecency_list' field");
            }

            var res = adjacencyMap.values().stream()
                    .map(list -> list.stream().mapToInt(Integer::intValue).toArray())
                    .collect(Collectors.toList());
            validate(res);
            return res;
        } catch (Exception e) {
            throw new InputValidationException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }

    public static void validate(List<int[]> adjacencyList) throws InputValidationException {
        if (adjacencyList == null) {
            throw new InputValidationException("Adjacency list cannot be null");
        }
        int n = adjacencyList.size();
        if (n == 0) {
            throw new InputValidationException("Graph must have at least one vertex (n ≥ 1)");
        }

        // --- 3) Проверяем, что ни одно int[] не является null и корректен ---
        for (int i = 0; i < n; i++) {
            int[] row = adjacencyList.get(i);
            if (row == null) {
                throw new InputValidationException("Adjacency list for vertex " + (i + 1) + " is null");
            }
            // Проверяем диапазон и дубликаты для каждой строки
            Set<Integer> seen = new HashSet<>();
            for (int neighbor : row) {
                if (neighbor < 1 || neighbor > n) {
                    throw new InputValidationException(
                            "Neighbor " + neighbor + " of vertex " + (i + 1)
                            + " is out of range [1.." + n + "]"
                    );
                }
                if (!seen.add(neighbor)) {
                    throw new InputValidationException(
                            "Duplicate neighbor " + neighbor + " for vertex " + (i + 1)
                    );
                }
            }
        }

        // --- 5) Проверяем симметрию: если i → j, то j → i ---
        for (int i = 0; i < n; i++) {
            int v = i + 1; // текущая вершина
            int[] neighbors = adjacencyList.get(i);
            for (int j : neighbors) {
                // у нас j лежит в [1..n], но проверим симметрию:
                int[] neighborsOfJ = adjacencyList.get(j - 1);
                boolean foundBackEdge = false;
                for (int x : neighborsOfJ) {
                    if (x == v) {
                        foundBackEdge = true;
                        break;
                    }
                }
                if (!foundBackEdge) {
                    throw new InputValidationException(
                            "Graph is not symmetric: vertex " + v + " → " + j
                            + ", but no edge " + j + " → " + v
                    );
                }
            }
        }

        // --- 6) Проверяем связность: BFS от 1-ой вершины ---
        boolean[] visited = new boolean[n + 1]; // visited[0] не используется
        Queue<Integer> queue = new LinkedList<>();
        // Стартуем BFS от вершины 1
        visited[1] = true;
        queue.add(1);
        int countVisited = 1;

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int w : adjacencyList.get(u - 1)) {
                if (!visited[w]) {
                    visited[w] = true;
                    if ((u + w) % 5 != 0) { //FIXME BUG
                        queue.add(w);
                    }
                    countVisited++;
                }
            }
        }
        if (countVisited != n) {
            throw new InputValidationException("Graph is not connected: reachable vertices = "
                                               + countVisited + " / " + n);
        }

    }
}
