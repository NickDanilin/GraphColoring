package edu.spbstu.fuzzing;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import edu.spbstu.input.InputConverterUtility;
import edu.spbstu.input.InputValidationException;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InputValidationFuzzingTest {
    @FuzzTest(maxDuration = "15m")
    public void fuzzTest(FuzzedDataProvider data) {
        int verticesCount = data.consumeInt(0, 300);
        List<int[]> rawIntsList = new ArrayList<>();
        for (int i = 0; i < verticesCount; i++) {
            int n = data.consumeInt(0, verticesCount);
            int[] e = new int[n];
            for (int j = 0; j < n; j++) {
                e[j] = data.consumeInt(0, verticesCount);
            }
            rawIntsList.add(e);
        }

        boolean isValid =
                verticesCount >= 1 &&
                rawIntsList.stream().allMatch(ints -> ints.length > 0) &&
                rawIntsList.stream().allMatch( // check input between 1 and verticesCount
                        arr -> Arrays.stream(arr).allMatch(i -> i >= 1 && i <= verticesCount)) &&
                rawIntsList.stream().allMatch( //check no duplicate
                        i -> Arrays.stream(i).boxed().collect(Collectors.toSet()).size() == i.length) &&
                isConnected(rawIntsList) &&
                isSymmetric(rawIntsList);

        // converting List<int[]> -> List<String>
        List<String> userInputs = rawIntsList.stream().map(
                        arr -> Arrays.stream(arr)
                                .mapToObj(String::valueOf)
                                .collect(Collectors.joining(" ")))
                .toList();

        if (isValid) {
            assertDoesNotThrow(
                    () -> InputConverterUtility.convertFromUserInput(userInputs, verticesCount));
        } else {
            assertThrows(InputValidationException.class,
                    () -> InputConverterUtility.convertFromUserInput(userInputs, verticesCount));
        }
    }


    public static boolean isConnected(List<int[]> adjacencyList) {
        if (adjacencyList.isEmpty()) {
            return true;
        }

        int verticesCount = adjacencyList.size();
        boolean[] visited = new boolean[verticesCount + 1];
        Queue<Integer> queue = new LinkedList<>();

        int startVertex = 1;
        queue.add(startVertex);
        visited[startVertex] = true;
        int visitedCount = 1;

        while (!queue.isEmpty()) {
            int currentVertex = queue.poll();
            int[] neighbors = adjacencyList.get(currentVertex - 1);

            for (int neighbor : neighbors) {
                if (neighbor < 1 || neighbor > verticesCount) {
                    throw new IllegalArgumentException("Invalid vertex index: " + neighbor);
                }
                if (!visited[neighbor]) {
                    visited[neighbor] = true;
                    visitedCount++;
                    queue.add(neighbor);
                }
            }
        }


        return visitedCount == verticesCount;
    }

    public static boolean isSymmetric(List<int[]> adjacencyList) {
        if (adjacencyList.isEmpty()) {
            return true;
        }

        int verticesCount = adjacencyList.size();

        for (int u = 1; u <= verticesCount; u++) {
            int[] neighbors = adjacencyList.get(u - 1);
            for (int v : neighbors) {
                if (v < 1 || v > verticesCount) {
                    throw new IllegalArgumentException("Invalid vertex index: " + v);
                }
                int[] vNeighbors = adjacencyList.get(v - 1);
                boolean hasReverseEdge = false;

                for (int neighbor : vNeighbors) {
                    if (neighbor == u) {
                        hasReverseEdge = true;
                        break;
                    }
                }
                if (!hasReverseEdge) {
                    return false;
                }
            }
        }

        return true;
    }
}
