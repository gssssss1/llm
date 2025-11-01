package com.langgraph.web.service;

import com.langgraph.core.StateGraph;
import com.langgraph.edge.ConditionalEdge;
import com.langgraph.edge.Edge;
import com.langgraph.edge.ParallelEdge;
import com.langgraph.edge.StaticEdge;
import com.langgraph.state.State;
import com.langgraph.viewer.GraphViewer;
import com.langgraph.web.model.GraphInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class GraphService {
    
    private final Map<String, StateGraph<?>> graphs = new ConcurrentHashMap<>();
    private final AtomicInteger graphIdCounter = new AtomicInteger(0);
    
    public <T extends State> String registerGraph(String name, StateGraph<T> graph) {
        String id = "graph-" + graphIdCounter.incrementAndGet();
        graphs.put(id, graph);
        return id;
    }
    
    public StateGraph<?> getGraph(String id) {
        return graphs.get(id);
    }
    
    public Map<String, StateGraph<?>> getAllGraphs() {
        return Map.copyOf(graphs);
    }
    
    public <T extends State> GraphInfo getGraphInfo(String id) {
        StateGraph<T> graph = (StateGraph<T>) graphs.get(id);
        if (graph == null) {
            return null;
        }
        
        List<GraphInfo.NodeInfo> nodes = graph.getNodes().keySet().stream()
            .map(name -> new GraphInfo.NodeInfo(name, name, "node"))
            .toList();
        
        List<GraphInfo.EdgeInfo> edges = new ArrayList<>();
        int edgeId = 0;
        
        for (Edge<T> edge : graph.getEdges()) {
            if (edge instanceof StaticEdge<T> staticEdge) {
                edges.add(new GraphInfo.EdgeInfo(
                    "edge-" + (edgeId++),
                    staticEdge.from(),
                    staticEdge.to(),
                    "static",
                    ""
                ));
            } else if (edge instanceof ConditionalEdge<T> conditionalEdge) {
                edges.add(new GraphInfo.EdgeInfo(
                    "edge-" + (edgeId++),
                    conditionalEdge.from(),
                    "?",
                    "conditional",
                    "conditional"
                ));
            } else if (edge instanceof ParallelEdge<T> parallelEdge) {
                for (String target : parallelEdge.targets()) {
                    edges.add(new GraphInfo.EdgeInfo(
                        "edge-" + (edgeId++),
                        parallelEdge.from(),
                        target,
                        "parallel",
                        "parallel"
                    ));
                }
            }
        }
        
        var stats = GraphViewer.getStats(graph);
        GraphInfo.GraphStats graphStats = new GraphInfo.GraphStats(
            stats.nodeCount(),
            stats.edgeCount(),
            stats.staticEdges(),
            stats.conditionalEdges(),
            stats.parallelEdges()
        );
        
        return new GraphInfo(
            id,
            id,
            graph.getStartNode(),
            graph.getEndNode().orElse(null),
            nodes,
            edges,
            graphStats
        );
    }
    
    public <T extends State> String getMermaidDiagram(String id) {
        StateGraph<T> graph = (StateGraph<T>) graphs.get(id);
        if (graph == null) {
            return null;
        }
        return GraphViewer.toMermaid(graph);
    }
}
