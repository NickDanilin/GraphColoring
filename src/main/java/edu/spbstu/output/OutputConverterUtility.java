package edu.spbstu.output;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.spbstu.coloring.ColoredVertex;

import java.util.Map;

public class OutputConverterUtility {
    private OutputConverterUtility() {
    }

    public static String convertToString(Map<ColoredVertex, int[]> graph) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ColoredVertex, int[]> entry : graph.entrySet()) {
            ColoredVertex cv = entry.getKey();
            int[] neighbors = entry.getValue();

            sb.append("Vertex ")
                    .append(cv.getVertexId())
                    .append(": color = ")
                    .append(cv.getColor())
                    .append(", neighbors = [");

            if (neighbors != null && neighbors.length > 0) {
                for (int i = 0; i < neighbors.length; i++) {
                    sb.append(neighbors[i]);
                    if (i < neighbors.length - 1) {
                        sb.append(", ");
                    }
                }
            }
            sb.append("]\n");
        }
        return sb.toString();
    }

    public static String convertToJson(Map<ColoredVertex, int[]> graph) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        for (Map.Entry<ColoredVertex, int[]> entry : graph.entrySet()) {
            ColoredVertex cv = entry.getKey();
            int[] neighbors = entry.getValue();

            ObjectNode vertexNode = mapper.createObjectNode();
            vertexNode.put("color", cv.getColor());

            ArrayNode neighborsArray = mapper.createArrayNode();
            if (neighbors != null) {
                for (int v : neighbors) {
                    neighborsArray.add(v);
                }
            }
            vertexNode.set("neighbors", neighborsArray);

            root.set(String.valueOf(cv.getVertexId()), vertexNode);
        }

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to convert graph to JSON", e);
        }
    }
}
