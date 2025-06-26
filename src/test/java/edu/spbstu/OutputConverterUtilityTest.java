package edu.spbstu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.spbstu.coloring.ColoredVertex;
import edu.spbstu.output.OutputConverterUtility;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class OutputConverterUtilityTest {

    @Test
    void convertToString_emptyGraph_returnsEmptyString() {
        Map<ColoredVertex, int[]> emptyMap = new LinkedHashMap<>();
        String result = OutputConverterUtility.convertToString(emptyMap);
        assertThat(result, is(""));
    }

    @Test
    void convertToString_singleVertexWithoutNeighbors() {
        Map<ColoredVertex, int[]> graph = new LinkedHashMap<>();
        graph.put(new ColoredVertex(1, 3), new int[0]);

        String expected = "Vertex 1: color = 3, neighbors = []\n";
        String result = OutputConverterUtility.convertToString(graph);
        assertThat(result, is(expected));
    }

    @Test
    void convertToString_multipleVertices_formatsCorrectly() {
        Map<ColoredVertex, int[]> graph = new LinkedHashMap<>();
        graph.put(new ColoredVertex(1, 2), new int[]{2, 3});
        graph.put(new ColoredVertex(2, 1), new int[]{1});
        graph.put(new ColoredVertex(3, 2), new int[]{1});

        String result = OutputConverterUtility.convertToString(graph);
        String[] lines = result.split("\n");
        assertThat(lines.length, is(3));
        assertThat(lines[0], is("Vertex 1: color = 2, neighbors = [2, 3]"));
        assertThat(lines[1], is("Vertex 2: color = 1, neighbors = [1]"));
        assertThat(lines[2], is("Vertex 3: color = 2, neighbors = [1]"));
    }

    @Test
    void convertToJson_emptyGraph_returnsEmptyJsonObject() throws Exception {
        Map<ColoredVertex, int[]> emptyMap = new LinkedHashMap<>();
        String json = OutputConverterUtility.convertToJson(emptyMap);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(json);
        assertTrue(node.isObject());
        assertThat(node.size(), is(0));
    }

    @Test
    void convertToJson_singleVertexWithoutNeighbors() throws Exception {
        Map<ColoredVertex, int[]> graph = new LinkedHashMap<>();
        graph.put(new ColoredVertex(5, 1), new int[0]);

        String json = OutputConverterUtility.convertToJson(graph);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        assertTrue(root.has("5"));
        JsonNode vertexNode = root.get("5");
        assertThat(vertexNode.get("color").asInt(), is(1));
        JsonNode neighborsArray = vertexNode.get("neighbors");
        assertTrue(neighborsArray.isArray());
        assertThat(neighborsArray.size(), is(0));
    }

    @Test
    void convertToJson_multipleVertices_buildsCorrectJson() throws Exception {
        Map<ColoredVertex, int[]> graph = new LinkedHashMap<>();
        graph.put(new ColoredVertex(1, 2), new int[]{2, 3});
        graph.put(new ColoredVertex(2, 1), new int[]{1});
        graph.put(new ColoredVertex(3, 3), new int[]{1, 2});

        String json = OutputConverterUtility.convertToJson(graph);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        assertThat(root.size(), is(3));

        JsonNode v1 = root.get("1");
        assertThat(v1.get("color").asInt(), is(2));
        assertThat(v1.get("neighbors").isArray(), is(true));
        assertThat(v1.get("neighbors"), containsInAnyOrder(
                mapper.getNodeFactory().numberNode(2),
                mapper.getNodeFactory().numberNode(3)
        ));

        JsonNode v2 = root.get("2");
        assertThat(v2.get("color").asInt(), is(1));
        assertThat(v2.get("neighbors").size(), is(1));
        assertThat(v2.get("neighbors").get(0).asInt(), is(1));

        JsonNode v3 = root.get("3");
        assertThat(v3.get("color").asInt(), is(3));
        assertThat(v3.get("neighbors"), containsInAnyOrder(
                mapper.getNodeFactory().numberNode(1),
                mapper.getNodeFactory().numberNode(2)
        ));
    }

    @Test
    void convertToJson_nullNeighbors_areHandledAsEmptyArray() throws Exception {
        Map<ColoredVertex, int[]> graph = new LinkedHashMap<>();
        graph.put(new ColoredVertex(10, 5), null);

        String json = OutputConverterUtility.convertToJson(graph);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        assertTrue(root.has("10"));
        JsonNode v10 = root.get("10");
        assertThat(v10.get("color").asInt(), is(5));
        JsonNode neighborsArray = v10.get("neighbors");
        assertTrue(neighborsArray.isArray());
        assertThat(neighborsArray.size(), is(0));
    }
}
