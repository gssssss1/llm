package com.langgraph.viewer;

public record GraphStats(
    int nodeCount,
    int edgeCount,
    int staticEdges,
    int conditionalEdges,
    int parallelEdges
) {
    @Override
    public String toString() {
        return String.format(
            "Graph Statistics:\n" +
            "  Nodes: %d\n" +
            "  Edges: %d\n" +
            "    - Static: %d\n" +
            "    - Conditional: %d\n" +
            "    - Parallel: %d",
            nodeCount, edgeCount, staticEdges, conditionalEdges, parallelEdges
        );
    }
}
