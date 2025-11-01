package com.langgraph.web.model;

import java.time.Instant;
import java.util.Map;

public record ExecutionEvent(
    String executionId,
    String eventType,
    String nodeName,
    Instant timestamp,
    Map<String, Object> data,
    String message
) {
    public enum EventType {
        EXECUTION_STARTED,
        NODE_STARTED,
        NODE_COMPLETED,
        NODE_FAILED,
        EXECUTION_COMPLETED,
        EXECUTION_FAILED
    }
    
    public static ExecutionEvent executionStarted(String executionId, Map<String, Object> initialState) {
        return new ExecutionEvent(
            executionId,
            EventType.EXECUTION_STARTED.name(),
            null,
            Instant.now(),
            initialState,
            "Execution started"
        );
    }
    
    public static ExecutionEvent nodeStarted(String executionId, String nodeName, Map<String, Object> state) {
        return new ExecutionEvent(
            executionId,
            EventType.NODE_STARTED.name(),
            nodeName,
            Instant.now(),
            state,
            "Node execution started: " + nodeName
        );
    }
    
    public static ExecutionEvent nodeCompleted(String executionId, String nodeName, Map<String, Object> state) {
        return new ExecutionEvent(
            executionId,
            EventType.NODE_COMPLETED.name(),
            nodeName,
            Instant.now(),
            state,
            "Node execution completed: " + nodeName
        );
    }
    
    public static ExecutionEvent nodeFailed(String executionId, String nodeName, String error) {
        return new ExecutionEvent(
            executionId,
            EventType.NODE_FAILED.name(),
            nodeName,
            Instant.now(),
            Map.of("error", error),
            "Node execution failed: " + nodeName
        );
    }
    
    public static ExecutionEvent executionCompleted(String executionId, Map<String, Object> finalState) {
        return new ExecutionEvent(
            executionId,
            EventType.EXECUTION_COMPLETED.name(),
            null,
            Instant.now(),
            finalState,
            "Execution completed successfully"
        );
    }
    
    public static ExecutionEvent executionFailed(String executionId, String error) {
        return new ExecutionEvent(
            executionId,
            EventType.EXECUTION_FAILED.name(),
            null,
            Instant.now(),
            Map.of("error", error),
            "Execution failed"
        );
    }
}
