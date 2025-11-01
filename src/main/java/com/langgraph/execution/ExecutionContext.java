package com.langgraph.execution;

import com.langgraph.state.State;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExecutionContext<T extends State> {
    private final String executionId;
    private final Instant startTime;
    private final List<String> executedNodes;
    private final AtomicBoolean interrupted;
    private T currentState;
    private String currentNode;
    private int stepCount;
    
    public ExecutionContext(T initialState) {
        this.executionId = UUID.randomUUID().toString();
        this.startTime = Instant.now();
        this.currentState = initialState;
        this.executedNodes = new ArrayList<>();
        this.interrupted = new AtomicBoolean(false);
        this.stepCount = 0;
    }
    
    public ExecutionContext(String executionId, T initialState, String currentNode) {
        this.executionId = executionId;
        this.startTime = Instant.now();
        this.currentState = initialState;
        this.currentNode = currentNode;
        this.executedNodes = new ArrayList<>();
        this.interrupted = new AtomicBoolean(false);
        this.stepCount = 0;
    }
    
    public String getExecutionId() {
        return executionId;
    }
    
    public Instant getStartTime() {
        return startTime;
    }
    
    public T getCurrentState() {
        return currentState;
    }
    
    public void setCurrentState(T state) {
        this.currentState = state;
    }
    
    public String getCurrentNode() {
        return currentNode;
    }
    
    public void setCurrentNode(String node) {
        this.currentNode = node;
    }
    
    public List<String> getExecutedNodes() {
        return List.copyOf(executedNodes);
    }
    
    public void addExecutedNode(String node) {
        executedNodes.add(node);
    }
    
    public boolean isInterrupted() {
        return interrupted.get();
    }
    
    public void interrupt() {
        interrupted.set(true);
    }
    
    public int getStepCount() {
        return stepCount;
    }
    
    public void incrementStepCount() {
        stepCount++;
    }
}
