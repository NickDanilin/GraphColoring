package edu.spbstu;

import edu.spbstu.coloring.ColoredVertex;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class ColoredVertexTest {

    @Test
    void constructor_validArguments_createsInstance() {
        ColoredVertex cv = new ColoredVertex(5, 3);
        assertEquals(5, cv.getVertexId());
        assertEquals(3, cv.getColor());
    }

    @Test
    void constructor_vertexIdLessThanOne_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new ColoredVertex(0, 1)
        );
        assertThat(ex.getMessage(), containsString("vertexId must be >= 1"));
    }

    @Test
    void constructor_colorLessThanOne_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new ColoredVertex(1, 0)
        );
        assertThat(ex.getMessage(), containsString("color must be >= 1"));
    }

    @Test
    void equals_sameReference_returnsTrue() {
        ColoredVertex cv = new ColoredVertex(2, 4);
        assertTrue(cv.equals(cv));
    }

    @Test
    void equals_null_returnsFalse() {
        ColoredVertex cv = new ColoredVertex(2, 4);
        assertFalse(cv.equals(null));
    }

    @Test
    void equals_differentClass_returnsFalse() {
        ColoredVertex cv = new ColoredVertex(2, 4);
        assertFalse(cv.equals("some string"));
    }

    @Test
    void equals_sameValues_returnsTrue() {
        ColoredVertex a = new ColoredVertex(3, 5);
        ColoredVertex b = new ColoredVertex(3, 5);
        assertEquals(a, b);
        assertEquals(b, a); // symmetry
    }

    @Test
    void equals_differentVertexId_returnsFalse() {
        ColoredVertex a = new ColoredVertex(3, 5);
        ColoredVertex b = new ColoredVertex(4, 5);
        assertNotEquals(a, b);
    }

    @Test
    void equals_differentColor_returnsFalse() {
        ColoredVertex a = new ColoredVertex(3, 5);
        ColoredVertex b = new ColoredVertex(3, 6);
        assertNotEquals(a, b);
    }

    @Test
    void hashCode_equalObjects_sameHashCode() {
        ColoredVertex a = new ColoredVertex(7, 2);
        ColoredVertex b = new ColoredVertex(7, 2);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void hashCode_differentObjects_differentHashCodes() {
        ColoredVertex a = new ColoredVertex(7, 2);
        ColoredVertex b = new ColoredVertex(2, 7);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toString_containsVertexIdAndColor() {
        ColoredVertex cv = new ColoredVertex(10, 4);
        String s = cv.toString();
        assertThat(s, containsString("Vertex{10}"));
        assertThat(s, containsString("Color{4}"));
        assertThat(s, startsWith("Vertex{10}"));
        assertThat(s, endsWith("Color{4}"));
    }
}
