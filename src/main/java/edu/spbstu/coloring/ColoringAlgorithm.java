package edu.spbstu.coloring;

import java.util.List;
import java.util.Map;

public interface ColoringAlgorithm {
    Map<ColoredVertex, int[]> colorGraph(List<int[]> graph);
}
