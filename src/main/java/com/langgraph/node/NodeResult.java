package com.langgraph.node;

import com.langgraph.state.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record NodeResult<T extends State>(
    T updatedState,
    List<String> nextNodes
) {
    public NodeResult {
        if (updatedState == null) {
            throw new IllegalArgumentException("Updated state cannot be null");
        }
        if (nextNodes == null) {
            nextNodes = new ArrayList<>();
        }
    }
    
    public NodeResult(T updatedState) {
        this(updatedState, new ArrayList<>());
    }
    
    public NodeResult(T updatedState, String... nextNodes) {
        this(updatedState, Arrays.asList(nextNodes));
    }
    
    public boolean hasNextNodes() {
        return !nextNodes.isEmpty();
    }
}
