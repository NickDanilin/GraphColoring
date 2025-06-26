package edu.spbstu;

import edu.spbstu.input.InputConverterUtility;
import edu.spbstu.input.InputValidationException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class InputConverterUtilityTest {
    //---- convertFromUserInput tests ----

    @Test
    void convertFromUserInput_nullList_throws() {
        assertThrows(InputValidationException.class, () ->
                InputConverterUtility.convertFromUserInput(null, 3)
        );
    }

    @Test
    void convertFromUserInput_emptyList_throws() {
        List<String> inputs = Collections.emptyList();
        assertThrows(InputValidationException.class, () ->
                InputConverterUtility.convertFromUserInput(inputs, 3)
        );
    }

    @Test
    void convertFromUserInput_sizeMismatch_throws() {
        List<String> inputs = List.of("1 2", "2 3");
        assertThrows(InputValidationException.class, () ->
                InputConverterUtility.convertFromUserInput(inputs, 3)
        );
    }

    @Test
    void convertFromUserInput_containsEmptyString_throws() {
        List<String> inputs = List.of("1 2", "", "2");
        assertThrows(InputValidationException.class, () ->
                InputConverterUtility.convertFromUserInput(inputs, 3)
        );
    }

    @Test
    void convertFromUserInput_nonNumeric_throws() {
        List<String> inputs = List.of("1 a", "2", "3");
        InputValidationException ex = assertThrows(InputValidationException.class, () ->
                InputConverterUtility.convertFromUserInput(inputs, 3)
        );
        assertThat(ex.getMessage(), containsString("only numbers and spaces"));
    }

    @Test
    void convertFromUserInput_valueOutOfRange_throws() {
        List<String> inputs = List.of("1 4", "1", "2");
        InputValidationException ex = assertThrows(InputValidationException.class, () ->
                InputConverterUtility.convertFromUserInput(inputs, 3)
        );
        assertThat(ex.getMessage(), containsString("between 1 and 3"));
    }

    @Test
    void convertFromUserInput_duplicateNeighbor_throws() {
        // "1 1" duplicates neighbor 1 for vertex 1
        List<String> inputs = List.of("1 1", "1", "1");
        InputValidationException ex = assertThrows(InputValidationException.class, () ->
                InputConverterUtility.convertFromUserInput(inputs, 3)
        );
        assertThat(ex.getMessage(), containsString("Duplicate neighbor 1"));
    }

    @Test
    void convertFromUserInput_nonSymmetric_throws() {
        // Vertex1→2, but Vertex2→[] so not symmetric
        List<String> inputs = List.of("2", "", ""); // n=3, but disconnected as well
        InputValidationException ex = assertThrows(InputValidationException.class, () ->
                InputConverterUtility.convertFromUserInput(inputs, 3)
        );
        assertThat(ex.getMessage(), anyOf(
                containsString("empty"),
                containsString("null")
        ));
    }

    @Test
    void convertFromUserInput_disconnected_throws() {
        // Each vertex has empty neighbors → disconnected
        List<String> inputs = List.of("", "", "");
        InputValidationException ex = assertThrows(InputValidationException.class, () ->
                InputConverterUtility.convertFromUserInput(inputs, 3)
        );
        assertThat(ex.getMessage(), containsString("empty"));
    }

    @Test
    void convertFromUserInput_validSmallGraph_returnsList() throws Exception {
        // A simple triangle: 1<->2<->3<->1
        List<String> inputs = List.of("2 3", "1 3", "1 2");
        List<int[]> result = InputConverterUtility.convertFromUserInput(inputs, 3);
        assertNotNull(result);
        assertThat(result.size(), Matchers.is(3));
        assertThat(toIntegerArray(result.get(0)), arrayContainingInAnyOrder(2, 3));
        assertThat(toIntegerArray(result.get(1)), arrayContainingInAnyOrder(1, 3));
        assertThat(toIntegerArray(result.get(2)), arrayContainingInAnyOrder(1, 2));
    }

    private Integer[] toIntegerArray(int[] array) {
        return java.util.Arrays.stream(array).boxed().toArray(Integer[]::new);
    }
    //---- convertFromJson tests ----

    @Test
    void convertFromJson_invalidJsonSyntax_throws() {
        String badJson = "{ adjacency_list: [1,2], }"; // missing quotes
        InputValidationException ex = assertThrows(InputValidationException.class, () ->
                InputConverterUtility.convertFromJson(badJson)
        );
        assertThat(ex.getMessage(), containsString("Failed to parse JSON"));
    }

    @Test
    void convertFromJson_missingField_throws() {
        String json = """
            { "wrong_key": { "1":[2], "2":[1] } }
            """;
        InputValidationException ex = assertThrows(InputValidationException.class, () ->
                InputConverterUtility.convertFromJson(json)
        );
        assertThat(ex.getMessage(), containsString("non-empty 'adjecency_list'"));
    }

    @Test
    void convertFromJson_emptyAdjacency_throws() {
        String json = """
            { "adjacency_list": {} }
            """;
        InputValidationException ex = assertThrows(InputValidationException.class, () ->
                InputConverterUtility.convertFromJson(json)
        );
        assertThat(ex.getMessage(), containsString("non-empty 'adjecency_list'"));
    }

    @Test
    void convertFromJson_nonSymmetric_throws() {
        String json = """
            {
              "adjacency_list": {
                "1":[2],
                "2":[]
              }
            }
            """;
        InputValidationException ex = assertThrows(InputValidationException.class, () ->
                InputConverterUtility.convertFromJson(json)
        );
        assertThat(ex.getMessage(), containsString("not symmetric"));
    }

    @Test
    void convertFromJson_disconnected_throws() {
        String json = """
            {
              "adjacency_list": {
                "1":[],
                "2":[]
              }
            }
            """;
        InputValidationException ex = assertThrows(InputValidationException.class, () ->
                InputConverterUtility.convertFromJson(json)
        );
        assertThat(ex.getMessage(), containsString("not connected"));
    }

    @Test
    void convertFromJson_validGraph_returnsList() throws Exception {
        String json = """
            {
              "adjacency_list": {
                "1":[2,3],
                "2":[1,3],
                "3":[1,2]
              }
            }
            """;
        List<int[]> result = InputConverterUtility.convertFromJson(json);
        assertNotNull(result);
        // order of values in List is unspecified (Map.values()). We check each row individually.
        boolean found123 = false;
        for (int[] row : result) {
            if (row.length == 2 &&
                ((row[0] == 2 && row[1] == 3) || (row[0] == 3 && row[1] == 2))) {
                found123 = true;
            }
        }
        assertTrue(found123, "One of rows must be [2,3]");
    }

    //---- validate tests ----

    static Stream<Arguments> invalidAdjacencyProvider() {
        return Stream.of(
                Arguments.of(null, "cannot be null"),
                Arguments.of(Collections.emptyList(), "at least one vertex"),
                // adjacencyList with a null row
                Arguments.of(Arrays.asList(new int[]{1}, null), "is null"),
                // neighbor out of range: vertex 1 has neighbor 3 but n=2
                Arguments.of(Arrays.asList(new int[]{2}, new int[]{3}), "out of range"),
                // duplicate neighbor inside a row
                Arguments.of(Arrays.asList(new int[]{2,2}, new int[]{1,2}), "Duplicate neighbor 2"),
                // non-symmetric: 1→2, 2→[]
                Arguments.of(Arrays.asList(new int[]{2}, new int[]{}), "not symmetric"),
                // disconnected: 1->[], 2->[]
                Arguments.of(Arrays.asList(new int[]{}, new int[]{}), "not connected")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidAdjacencyProvider")
    void validate_invalidAdjacency_throws(List<int[]> adjacency, String expectedMessageFragment) {
        InputValidationException ex = assertThrows(InputValidationException.class, () ->
                InputConverterUtility.validate(adjacency)
        );
        assertThat(ex.getMessage(), containsString(expectedMessageFragment));
    }

    @Test
    void validate_validAdjacency_noException() {
        // simple connected, symmetric 3-vertex
        List<int[]> adj = Arrays.asList(
                new int[]{2,3},
                new int[]{1,3},
                new int[]{1,2}
        );
        assertDoesNotThrow(() -> InputConverterUtility.validate(adj));
    }

    //---- combined flow ----

    @Test
    void fullFlow_convertFromUserInput_then_validate_assumptionLargeGraph() {
        // Предположим, что для больших графов используется некий оптимизированный путь
        List<String> inputs = new ArrayList<>();
        int n = 60;
        // Построим цепной граф 1-2-3-...-60
        for (int i = 1; i <= n; i++) {
            if (i == 1) {
                inputs.add("2");
            } else if (i == n) {
                inputs.add(String.valueOf(n - 1));
            } else {
                inputs.add((i - 1) + " " + (i + 1));
            }
        }
        // Проверим, что n > 50
        Assumptions.assumeTrue(n > 50);
        // Убедимся, что конвертация не выбросит
        List<int[]> adj = assertDoesNotThrow(() ->
                InputConverterUtility.convertFromUserInput(inputs, n)
        );
        // Теперь они должны пройти validate
        assertDoesNotThrow(() -> InputConverterUtility.validate(adj));
        // И граф действительно содержит 60 строк
        assertThat(adj.size(), is(n));
    }
}
