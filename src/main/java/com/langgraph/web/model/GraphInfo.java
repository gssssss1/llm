package com.langgraph.web.model;

import java.util.List;
import java.util.Map;

public record GraphInfo(
    String id,
    String name,
    String startNode,
    String endNode,
    List<NodeInfo> nodes,
    List<EdgeInfo> edges,
    GraphStats stats
) {
    public record NodeInfo(
        String id,
        String name,
        String type
    ) {}
    
    public record EdgeInfo(
        String id,
        String from,
        String to,
        String type,
        String label
    ) {}
    
    public record GraphStats(
        int nodeCount,
        int edgeCount,
        int staticEdges,
        int conditionalEdges,
        int parallelEdges
    ) {}
}
