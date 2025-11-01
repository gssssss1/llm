package com.langgraph.edge;

import com.langgraph.state.State;

import java.util.function.Function;

public record ConditionalEdge<T extends State>(
    String from,
    Function<T, String> condition
) implements Edge<T> {
    
    public ConditionalEdge {
        if (from == null || from.isBlank()) {
            throw new IllegalArgumentException("From node cannot be null or blank");
        }
        if (condition == null) {
            throw new IllegalArgumentException("Condition function cannot be null");
        }
    }
    
    @Override
    public String to() {
        return null;
    }
    
    public String evaluateTo(T state) {
        return condition.apply(state);
    }
}
