package edu.spbstu.BDD.steps;

import edu.spbstu.LoadTest;
import edu.spbstu.coloring.ColoredVertex;
import edu.spbstu.coloring.ColoringAlgorithm;
import edu.spbstu.coloring.DSaturColoring;
import edu.spbstu.coloring.RLFColoring;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ColoringDefs {
    private ColoringAlgorithm algorithm;
    private List<int[]> adjacencyList;
    private Map<ColoredVertex, int[]> result;

    @Given("I have selected {string} algorithm")
    public void iHaveSelectedAlgorithm(String alg) {
        switch (alg) {
            case "DSatur":
                algorithm = new DSaturColoring();
                break;
            case "RLF":
                algorithm = new RLFColoring();
                break;
            default:
                throw new IllegalArgumentException("Unexpected algorithm: " + alg);
        }

    }

    @And("I have adjacency list")
    public void iHaveAdjacencyList(DataTable dataTable) {
        adjacencyList = dataTable.asList().stream().map(
                s-> Arrays.stream(s.split(" ")).mapToInt(Integer::parseInt).toArray()
        ).toList();
    }

    @When("I get coloring result")
    public void iGetColoringResult() {
        long startTime = System.currentTimeMillis();
        result = algorithm.colorGraph(adjacencyList);
        long endTime = System.currentTimeMillis();
        long durationMs = endTime - startTime;

        if (durationMs >= 1000) {
            double durationSec = durationMs / 1000.0;
            System.out.printf("Execution time: %.2f seconds%n", durationSec);
        } else {
            System.out.printf("Execution time: %d milliseconds%n", durationMs);
        }

    }

    @Then("Coloring is correct")
    public void coloringIsCorrect() {
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

    @And("Graph has {int} distinct colors")
    public void graphHasDistinctColors(int n) {
        var colorSet = result.keySet().stream().map(ColoredVertex::getColor).collect(Collectors.toSet());
        Assertions.assertEquals(n, colorSet.size());
    }

    @And("I get adjacency list from file {string}")
    public void iGetAdjacencyListFromFile(String path) {
        System.out.println(path);
        var in = LoadTest.class.getResourceAsStream(path);
        assert in != null;
        var reader = new BufferedReader(new InputStreamReader(in));
        adjacencyList = reader.lines().map(
                s -> Arrays.stream(s.split(" ")).mapToInt(Integer::parseInt).toArray()
        ).toList();
    }
}
