package com.llm.config;

import java.time.Duration;

/**
 * Execution configuration for controlling timeouts, retries, rate limits and circuit breakers.
 */
public class ExecutionConfig {

    private final Duration timeout;
    private final int maxRetries;
    private final Duration retryDelay;
    private final Integer rateLimit;
    private final boolean circuitBreakerEnabled;
    private final int circuitBreakerThreshold;

    private ExecutionConfig(Builder builder) {
        this.timeout = builder.timeout;
        this.maxRetries = builder.maxRetries;
        this.retryDelay = builder.retryDelay;
        this.rateLimit = builder.rateLimit;
        this.circuitBreakerEnabled = builder.circuitBreakerEnabled;
        this.circuitBreakerThreshold = builder.circuitBreakerThreshold;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public Duration getRetryDelay() {
        return retryDelay;
    }

    public Integer getRateLimit() {
        return rateLimit;
    }

    public boolean isCircuitBreakerEnabled() {
        return circuitBreakerEnabled;
    }

    public int getCircuitBreakerThreshold() {
        return circuitBreakerThreshold;
    }

    public static ExecutionConfig defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for execution configuration.
     */
    public static class Builder {
        private Duration timeout = Duration.ofSeconds(60);
        private int maxRetries = 3;
        private Duration retryDelay = Duration.ofSeconds(1);
        private Integer rateLimit = null;
        private boolean circuitBreakerEnabled = false;
        private int circuitBreakerThreshold = 5;

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder retryDelay(Duration retryDelay) {
            this.retryDelay = retryDelay;
            return this;
        }

        public Builder rateLimit(Integer rateLimit) {
            this.rateLimit = rateLimit;
            return this;
        }

        public Builder circuitBreakerEnabled(boolean enabled) {
            this.circuitBreakerEnabled = enabled;
            return this;
        }

        public Builder circuitBreakerThreshold(int threshold) {
            this.circuitBreakerThreshold = threshold;
            return this;
        }

        public ExecutionConfig build() {
            return new ExecutionConfig(this);
        }
    }
}
