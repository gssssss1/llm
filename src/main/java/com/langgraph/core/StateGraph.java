package com.langgraph.core;

import com.langgraph.edge.ConditionalEdge;
import com.langgraph.edge.Edge;
import com.langgraph.edge.ParallelEdge;
import com.langgraph.edge.StaticEdge;
import com.langgraph.node.Node;
import com.langgraph.node.NodeFunction;
import com.langgraph.state.State;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StateGraph<T extends State> {
    private final Map<String, Node<T>> nodes;
    private final List<Edge<T>> edges;
    private final String startNode;
    private String endNode;
    
    private StateGraph(Builder<T> builder) {
        this.nodes = Map.copyOf(builder.nodes);
        this.edges = List.copyOf(builder.edges);
        this.startNode = builder.startNode;
        this.endNode = builder.endNode;
        
        if (startNode == null) {
            throw new IllegalStateException("Start node must be set");
        }
        if (!nodes.containsKey(startNode)) {
            throw new IllegalStateException("Start node '" + startNode + "' is not in the graph");
        }
        
        validateGraph();
    }
    
    private void validateGraph() {
        for (Edge<T> edge : edges) {
            if (!nodes.containsKey(edge.from())) {
                throw new IllegalStateException("Edge references non-existent source node: " + edge.from());
            }
            
            if (edge instanceof StaticEdge<T> staticEdge) {
                if (!nodes.containsKey(staticEdge.to())) {
                    throw new IllegalStateException("Edge references non-existent target node: " + staticEdge.to());
                }
            } else if (edge instanceof ParallelEdge<T> parallelEdge) {
                for (String target : parallelEdge.targets()) {
                    if (!nodes.containsKey(target)) {
                        throw new IllegalStateException("ParallelEdge references non-existent target node: " + target);
                    }
                }
            }
        }
    }
    
    public Map<String, Node<T>> getNodes() {
        return nodes;
    }
    
    public List<Edge<T>> getEdges() {
        return edges;
    }
    
    public String getStartNode() {
        return startNode;
    }
    
    public Optional<String> getEndNode() {
        return Optional.ofNullable(endNode);
    }
    
    public Node<T> getNode(String name) {
        Node<T> node = nodes.get(name);
        if (node == null) {
            throw new IllegalArgumentException("Node not found: " + name);
        }
        return node;
    }
    
    public List<Edge<T>> getEdgesFrom(String nodeName) {
        return edges.stream()
            .filter(e -> e.from().equals(nodeName))
            .toList();
    }
    
    public List<String> getNextNodes(String currentNode, T state) {
        List<Edge<T>> outgoingEdges = getEdgesFrom(currentNode);
        List<String> nextNodes = new ArrayList<>();
        
        for (Edge<T> edge : outgoingEdges) {
            if (edge instanceof StaticEdge<T> staticEdge) {
                nextNodes.add(staticEdge.to());
            } else if (edge instanceof ConditionalEdge<T> conditionalEdge) {
                String target = conditionalEdge.evaluateTo(state);
                if (target != null && !target.isBlank()) {
                    nextNodes.add(target);
                }
            } else if (edge instanceof ParallelEdge<T> parallelEdge) {
                nextNodes.addAll(parallelEdge.targets());
            }
        }
        
        return nextNodes;
    }
    
    public static <T extends State> Builder<T> builder() {
        return new Builder<>();
    }
    
    public static class Builder<T extends State> {
        private final Map<String, Node<T>> nodes = new ConcurrentHashMap<>();
        private final List<Edge<T>> edges = new ArrayList<>();
        private String startNode;
        private String endNode;
        
        public Builder<T> addNode(String name, NodeFunction<T> function) {
            nodes.put(name, new Node<>(name, function));
            return this;
        }
        
        public Builder<T> addNode(Node<T> node) {
            nodes.put(node.name(), node);
            return this;
        }
        
        public Builder<T> addEdge(String from, String to) {
            edges.add(new StaticEdge<>(from, to));
            return this;
        }
        
        public Builder<T> addConditionalEdge(String from, java.util.function.Function<T, String> condition) {
            edges.add(new ConditionalEdge<>(from, condition));
            return this;
        }
        
        public Builder<T> addParallelEdge(String from, String... targets) {
            edges.add(new ParallelEdge<>(from, Arrays.asList(targets)));
            return this;
        }
        
        public Builder<T> setStartNode(String startNode) {
            this.startNode = startNode;
            return this;
        }
        
        public Builder<T> setEndNode(String endNode) {
            this.endNode = endNode;
            return this;
        }
        
        public StateGraph<T> build() {
            return new StateGraph<>(this);
        }
    }
}
