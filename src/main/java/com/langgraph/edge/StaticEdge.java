package com.langgraph.edge;

import com.langgraph.state.State;

public record StaticEdge<T extends State>(
    String from,
    String to
) implements Edge<T> {
    public StaticEdge {
        if (from == null || from.isBlank()) {
            throw new IllegalArgumentException("From node cannot be null or blank");
        }
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("To node cannot be null or blank");
        }
    }
}
