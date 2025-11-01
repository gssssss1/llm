package com.langgraph.edge;

import com.langgraph.state.State;

import java.util.List;

public record ParallelEdge<T extends State>(
    String from,
    List<String> targets
) implements Edge<T> {
    
    public ParallelEdge {
        if (from == null || from.isBlank()) {
            throw new IllegalArgumentException("From node cannot be null or blank");
        }
        if (targets == null || targets.isEmpty()) {
            throw new IllegalArgumentException("Targets cannot be null or empty");
        }
        targets = List.copyOf(targets);
    }
    
    @Override
    public String to() {
        return null;
    }
}
