package com.langgraph.execution;

import java.time.Duration;

public record ExecutionConfig(
    boolean enableCheckpointing,
    boolean enableParallelExecution,
    Duration maxExecutionTime,
    int maxSteps
) {
    public static final ExecutionConfig DEFAULT = new ExecutionConfig(
        true,
        true,
        Duration.ofMinutes(30),
        1000
    );
    
    public ExecutionConfig {
        if (maxExecutionTime == null || maxExecutionTime.isNegative()) {
            throw new IllegalArgumentException("Max execution time must be positive");
        }
        if (maxSteps <= 0) {
            throw new IllegalArgumentException("Max steps must be positive");
        }
    }
}
