package com.langgraph.execution;

import com.langgraph.state.State;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record ExecutionResult<T extends State>(
    String executionId,
    T finalState,
    ExecutionStatus status,
    List<String> executedNodes,
    Instant startTime,
    Instant endTime,
    Optional<Throwable> error
) {
    public Duration duration() {
        return Duration.between(startTime, endTime);
    }
    
    public boolean isSuccessful() {
        return status == ExecutionStatus.COMPLETED;
    }
    
    public boolean isInterrupted() {
        return status == ExecutionStatus.INTERRUPTED;
    }
    
    public boolean hasFailed() {
        return status == ExecutionStatus.FAILED;
    }
}
