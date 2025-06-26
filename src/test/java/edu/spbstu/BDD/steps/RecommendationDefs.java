package edu.spbstu.BDD.steps;

import edu.spbstu.coloring.ColoredVertex;
import edu.spbstu.output.RecommendationUtility;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class RecommendationDefs {
    private Map<ColoredVertex, int[]> graph;
    private String[] recs;

    @Given("I have a large graph")
    public void iHaveALargeGraph() {
        int n = 60;
        graph = new LinkedHashMap<>();

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
    }


    @When("I ask for recommendations")
    public void iAskForRecommendations() {
        recs = RecommendationUtility.getRecommendation(graph);
    }


    @Then("I should see recommendations on using DSATUR for large graphs")
    public void iShouldSeeRecommendationsOnUsingDSATURForLargeGraphs() {
        Assertions.assertTrue(Arrays.stream(recs).anyMatch(s -> s.contains("эффективнее использовать DSATUR")));
    }

    @Given("I have a small graph")
    public void iHaveASmallGraph() {
        graph = new LinkedHashMap<>();
        graph.put(new ColoredVertex(1, 1), new int[]{2, 4});
        graph.put(new ColoredVertex(2, 2), new int[]{1, 3});
        graph.put(new ColoredVertex(3, 1), new int[]{2, 4});
        graph.put(new ColoredVertex(4, 2), new int[]{1, 3});
    }


    @Then("I should see recommendations on using RLF for small graphs")
    public void iShouldSeeRecommendationsOnUsingRLFForSmallGraphs() {
        Assertions.assertTrue(Arrays.stream(recs).anyMatch(s -> s.contains("можно попробовать RLF для компрессии")));
    }


    @Given("I have a large sparse graph")
    public void iHaveALargeSparseGraph() {
        int n = 60;
        graph = new LinkedHashMap<>();

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
    }

    @And("I should see recommendations on using DSATUR for very sparse graphs")
    public void iShouldSeeRecommendationsOnUsingDSATURForVerySparseGraphs() {
        Assertions.assertTrue(Arrays.stream(recs)
                .anyMatch(s -> s.contains("На очень разреженных и больших графах DSATUR обычно работает быстрее"))
        );
    }

    @Given("I have a small high density graph")
    public void iHaveASmallHighDensityGraph() {
        graph = new LinkedHashMap<>();
        graph.put(new ColoredVertex(1, 1), new int[]{2, 4});
        graph.put(new ColoredVertex(2, 2), new int[]{1, 3});
        graph.put(new ColoredVertex(3, 1), new int[]{2, 4});
        graph.put(new ColoredVertex(4, 2), new int[]{1, 3});
    }

    @And("I should see recommendations on using RLF for high density graphs")
    public void iShouldSeeRecommendationsOnUsingRLFForHighDensityGraphs() {
        Assertions.assertTrue(Arrays.stream(recs)
                .anyMatch(s -> s.contains("RLF может дать лучшее качество"))
        );

    }
}
