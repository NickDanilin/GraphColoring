package edu.spbstu;

import edu.spbstu.coloring.ColoredVertex;
import edu.spbstu.coloring.ColoringAlgorithm;
import edu.spbstu.coloring.DSaturColoring;
import edu.spbstu.coloring.RLFColoring;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class LoadTest {
    private static List<int[]> adjacencyList;

    @BeforeAll
    static void loadData(){
        var in = LoadTest.class.getResourceAsStream("/datasets/unitgherkincompar.txt");
        assert in != null;
        var reader = new BufferedReader(new InputStreamReader(in));
        adjacencyList = reader.lines().map(
                s -> Arrays.stream(s.split(" ")).mapToInt(Integer::parseInt).toArray()
        ).toList();
    }


    static Arguments[] algorithms() {
        return new Arguments[]{
                Arguments.of(new DSaturColoring()),
                Arguments.of(new RLFColoring())
        };
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    public void fromFile(ColoringAlgorithm algorithm) {
        long startTime = System.currentTimeMillis();
        var result = algorithm.colorGraph(adjacencyList);
        long endTime = System.currentTimeMillis();
        long durationMs = endTime - startTime;

        if (durationMs >= 1000) {
            double durationSec = durationMs / 1000.0;
            System.out.printf("Execution time: %.2f seconds%n", durationSec);
        } else {
            System.out.printf("Execution time: %d milliseconds%n", durationMs);
        }

        Map<Integer, Integer> colorMap = new HashMap<>();
        for (ColoredVertex cv : result.keySet()) {
            colorMap.put(cv.getVertexId(), cv.getColor());
        }

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
    }

}

