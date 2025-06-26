package edu.spbstu.coloring;

public class ColoredVertex {
    private final int vertexId;
    private final int color;


    public ColoredVertex(int vertexId, int color) {
        if (vertexId < 1) {
            throw new IllegalArgumentException("vertexId must be >= 1, but was " + vertexId);
        }
        if (color < 1) {
            throw new IllegalArgumentException("color must be >= 1, but was " + color);
        }
        this.vertexId = vertexId;
        this.color = color;
    }

    public int getVertexId() {
        return vertexId;
    }

    public int getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColoredVertex that = (ColoredVertex) o;
        return vertexId == that.vertexId && color == that.color;
    }

    @Override
    public int hashCode() {
        return 31 * vertexId + color;
    }

    @Override
    public String toString() {
        return "Vertex{" + vertexId + "}, Color{" + color + "}";
    }
}