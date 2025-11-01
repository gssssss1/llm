package com.langgraph.checkpoint;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.langgraph.state.State;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record Checkpoint<T extends State>(
    @JsonProperty("id") String id,
    @JsonProperty("executionId") String executionId,
    @JsonProperty("state") T state,
    @JsonProperty("currentNode") String currentNode,
    @JsonProperty("timestamp") Instant timestamp,
    @JsonProperty("metadata") Map<String, Object> metadata
) {
    
    @JsonCreator
    public Checkpoint {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (executionId == null) {
            throw new IllegalArgumentException("Execution ID cannot be null");
        }
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
        if (metadata != null) {
            metadata = Map.copyOf(metadata);
        }
    }
    
    public Checkpoint(String executionId, T state, String currentNode) {
        this(null, executionId, state, currentNode, null, null);
    }
    
    public Checkpoint(String executionId, T state, String currentNode, Map<String, Object> metadata) {
        this(null, executionId, state, currentNode, null, metadata);
    }
}
