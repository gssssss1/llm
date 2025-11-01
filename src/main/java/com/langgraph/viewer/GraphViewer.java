package com.langgraph.viewer;

import com.langgraph.core.StateGraph;
import com.langgraph.edge.ConditionalEdge;
import com.langgraph.edge.Edge;
import com.langgraph.edge.ParallelEdge;
import com.langgraph.edge.StaticEdge;
import com.langgraph.state.State;

import java.util.List;
import java.util.stream.Collectors;

public class GraphViewer {
    
    public static <T extends State> String toMermaid(StateGraph<T> graph) {
        StringBuilder sb = new StringBuilder();
        sb.append("graph TD\n");
        
        sb.append("    START((START))\n");
        sb.append("    START --> ").append(graph.getStartNode()).append("\n");
        
        graph.getNodes().keySet().forEach(nodeName -> {
            String nodeLabel = nodeName.replace("_", " ");
            sb.append("    ").append(nodeName).append("[\"").append(nodeLabel).append("\"]\n");
        });
        
        for (Edge<T> edge : graph.getEdges()) {
            if (edge instanceof StaticEdge<T> staticEdge) {
                sb.append("    ").append(staticEdge.from())
                    .append(" --> ").append(staticEdge.to()).append("\n");
            } else if (edge instanceof ConditionalEdge<T> conditionalEdge) {
                sb.append("    ").append(conditionalEdge.from())
                    .append(" -->|conditional| ?").append(conditionalEdge.from()).append("\n");
            } else if (edge instanceof ParallelEdge<T> parallelEdge) {
                for (String target : parallelEdge.targets()) {
                    sb.append("    ").append(parallelEdge.from())
                        .append(" -.->|parallel| ").append(target).append("\n");
                }
            }
        }
        
        graph.getEndNode().ifPresent(endNode -> {
            sb.append("    ").append(endNode).append(" --> END((END))\n");
        });
        
        sb.append("\n");
        sb.append("    style START fill:#90EE90\n");
        sb.append("    style END fill:#FFB6C1\n");
        
        return sb.toString();
    }
    
    public static <T extends State> String toAscii(StateGraph<T> graph) {
        StringBuilder sb = new StringBuilder();
        sb.append("Graph Structure:\n");
        sb.append("================\n\n");
        
        sb.append("Start Node: ").append(graph.getStartNode()).append("\n");
        graph.getEndNode().ifPresent(end -> 
            sb.append("End Node: ").append(end).append("\n"));
        sb.append("\n");
        
        sb.append("Nodes:\n");
        graph.getNodes().keySet().forEach(node -> 
            sb.append("  - ").append(node).append("\n"));
        sb.append("\n");
        
        sb.append("Edges:\n");
        for (Edge<T> edge : graph.getEdges()) {
            if (edge instanceof StaticEdge<T> staticEdge) {
                sb.append("  ").append(staticEdge.from())
                    .append(" -> ").append(staticEdge.to()).append("\n");
            } else if (edge instanceof ConditionalEdge<T> conditionalEdge) {
                sb.append("  ").append(conditionalEdge.from())
                    .append(" -> [conditional]\n");
            } else if (edge instanceof ParallelEdge<T> parallelEdge) {
                sb.append("  ").append(parallelEdge.from())
                    .append(" -> [").append(String.join(", ", parallelEdge.targets()))
                    .append("] (parallel)\n");
            }
        }
        
        return sb.toString();
    }
    
    public static <T extends State> String toDot(StateGraph<T> graph) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph StateGraph {\n");
        sb.append("  rankdir=TB;\n");
        sb.append("  node [shape=box, style=rounded];\n\n");
        
        sb.append("  START [shape=circle, style=filled, fillcolor=lightgreen];\n");
        sb.append("  START -> ").append(graph.getStartNode()).append(";\n\n");
        
        for (Edge<T> edge : graph.getEdges()) {
            if (edge instanceof StaticEdge<T> staticEdge) {
                sb.append("  ").append(staticEdge.from())
                    .append(" -> ").append(staticEdge.to()).append(";\n");
            } else if (edge instanceof ConditionalEdge<T> conditionalEdge) {
                sb.append("  ").append(conditionalEdge.from())
                    .append(" -> CONDITION_").append(conditionalEdge.from())
                    .append(" [label=\"conditional\", style=dashed];\n");
            } else if (edge instanceof ParallelEdge<T> parallelEdge) {
                for (String target : parallelEdge.targets()) {
                    sb.append("  ").append(parallelEdge.from())
                        .append(" -> ").append(target)
                        .append(" [label=\"parallel\", style=dotted];\n");
                }
            }
        }
        
        graph.getEndNode().ifPresent(endNode -> {
            sb.append("\n  END [shape=circle, style=filled, fillcolor=lightpink];\n");
            sb.append("  ").append(endNode).append(" -> END;\n");
        });
        
        sb.append("}\n");
        return sb.toString();
    }
    
    public static <T extends State> GraphStats getStats(StateGraph<T> graph) {
        int nodeCount = graph.getNodes().size();
        int edgeCount = graph.getEdges().size();
        
        long conditionalEdges = graph.getEdges().stream()
            .filter(e -> e instanceof ConditionalEdge)
            .count();
        
        long parallelEdges = graph.getEdges().stream()
            .filter(e -> e instanceof ParallelEdge)
            .count();
        
        long staticEdges = graph.getEdges().stream()
            .filter(e -> e instanceof StaticEdge)
            .count();
        
        return new GraphStats(
            nodeCount,
            edgeCount,
            (int) staticEdges,
            (int) conditionalEdges,
            (int) parallelEdges
        );
    }
}
