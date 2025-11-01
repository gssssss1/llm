package com.langgraph.core;

import com.langgraph.edge.Edge;
import com.langgraph.node.Node;
import com.langgraph.state.State;

import java.util.*;
import java.util.stream.Collectors;

public class GraphUtils {
    
    private GraphUtils() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    public static <T extends State> boolean isAcyclic(StateGraph<T> graph) {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        
        for (String nodeName : graph.getNodes().keySet()) {
            if (hasCycle(graph, nodeName, visited, recursionStack)) {
                return false;
            }
        }
        
        return true;
    }
    
    private static <T extends State> boolean hasCycle(
        StateGraph<T> graph,
        String node,
        Set<String> visited,
        Set<String> recursionStack
    ) {
        if (recursionStack.contains(node)) {
            return true;
        }
        
        if (visited.contains(node)) {
            return false;
        }
        
        visited.add(node);
        recursionStack.add(node);
        
        List<Edge<T>> outgoingEdges = graph.getEdgesFrom(node);
        for (Edge<T> edge : outgoingEdges) {
            String target = edge.to();
            if (target != null && hasCycle(graph, target, visited, recursionStack)) {
                return true;
            }
        }
        
        recursionStack.remove(node);
        return false;
    }
    
    public static <T extends State> List<String> topologicalSort(StateGraph<T> graph) {
        Map<String, Integer> inDegree = new HashMap<>();
        
        for (String node : graph.getNodes().keySet()) {
            inDegree.put(node, 0);
        }
        
        for (Edge<T> edge : graph.getEdges()) {
            String target = edge.to();
            if (target != null) {
                inDegree.put(target, inDegree.getOrDefault(target, 0) + 1);
            }
        }
        
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }
        
        List<String> sorted = new ArrayList<>();
        
        while (!queue.isEmpty()) {
            String node = queue.poll();
            sorted.add(node);
            
            for (Edge<T> edge : graph.getEdgesFrom(node)) {
                String target = edge.to();
                if (target != null) {
                    int degree = inDegree.get(target) - 1;
                    inDegree.put(target, degree);
                    if (degree == 0) {
                        queue.offer(target);
                    }
                }
            }
        }
        
        return sorted;
    }
    
    public static <T extends State> Set<String> findReachableNodes(
        StateGraph<T> graph,
        String startNode
    ) {
        Set<String> reachable = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        
        queue.offer(startNode);
        reachable.add(startNode);
        
        while (!queue.isEmpty()) {
            String current = queue.poll();
            
            for (Edge<T> edge : graph.getEdgesFrom(current)) {
                String target = edge.to();
                if (target != null && !reachable.contains(target)) {
                    reachable.add(target);
                    queue.offer(target);
                }
            }
        }
        
        return reachable;
    }
    
    public static <T extends State> Set<String> findUnreachableNodes(StateGraph<T> graph) {
        Set<String> reachable = findReachableNodes(graph, graph.getStartNode());
        Set<String> allNodes = graph.getNodes().keySet();
        
        return allNodes.stream()
            .filter(node -> !reachable.contains(node))
            .collect(Collectors.toSet());
    }
    
    public static <T extends State> int calculateGraphDepth(StateGraph<T> graph) {
        return calculateDepth(graph, graph.getStartNode(), new HashSet<>());
    }
    
    private static <T extends State> int calculateDepth(
        StateGraph<T> graph,
        String node,
        Set<String> visited
    ) {
        if (visited.contains(node)) {
            return 0;
        }
        
        visited.add(node);
        
        List<Edge<T>> edges = graph.getEdgesFrom(node);
        if (edges.isEmpty()) {
            return 1;
        }
        
        int maxDepth = 0;
        for (Edge<T> edge : edges) {
            String target = edge.to();
            if (target != null) {
                int depth = calculateDepth(graph, target, new HashSet<>(visited));
                maxDepth = Math.max(maxDepth, depth);
            }
        }
        
        return maxDepth + 1;
    }
    
    public static <T extends State> Map<String, Integer> calculateNodeDepths(StateGraph<T> graph) {
        Map<String, Integer> depths = new HashMap<>();
        calculateNodeDepth(graph, graph.getStartNode(), 0, depths);
        return depths;
    }
    
    private static <T extends State> void calculateNodeDepth(
        StateGraph<T> graph,
        String node,
        int depth,
        Map<String, Integer> depths
    ) {
        if (depths.containsKey(node) && depths.get(node) <= depth) {
            return;
        }
        
        depths.put(node, depth);
        
        for (Edge<T> edge : graph.getEdgesFrom(node)) {
            String target = edge.to();
            if (target != null) {
                calculateNodeDepth(graph, target, depth + 1, depths);
            }
        }
    }
    
    public static <T extends State> boolean validateGraph(StateGraph<T> graph) {
        try {
            if (graph.getStartNode() == null) {
                return false;
            }
            
            if (!graph.getNodes().containsKey(graph.getStartNode())) {
                return false;
            }
            
            for (Edge<T> edge : graph.getEdges()) {
                if (!graph.getNodes().containsKey(edge.from())) {
                    return false;
                }
                
                String target = edge.to();
                if (target != null && !graph.getNodes().containsKey(target)) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
