package com.llm.exception;

/**
 * Exception thrown when rate limit is exceeded.
 */
public class RateLimitException extends ProviderException {

    private final Long retryAfterSeconds;

    public RateLimitException(String message) {
        super(429, message);
        this.retryAfterSeconds = null;
    }

    public RateLimitException(String message, Long retryAfterSeconds) {
        super(429, message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public Long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
