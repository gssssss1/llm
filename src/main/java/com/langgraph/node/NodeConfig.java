package com.langgraph.node;

import java.time.Duration;

public record NodeConfig(
    int maxRetries,
    Duration timeout,
    boolean retryOnException
) {
    public static final NodeConfig DEFAULT = new NodeConfig(0, Duration.ofMinutes(5), false);
    
    public NodeConfig {
        if (maxRetries < 0) {
            throw new IllegalArgumentException("Max retries cannot be negative");
        }
        if (timeout == null || timeout.isNegative()) {
            throw new IllegalArgumentException("Timeout must be positive");
        }
    }
}
