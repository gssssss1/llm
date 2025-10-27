package com.llm.exception;

/**
 * Exception thrown when a request times out.
 */
public class TimeoutException extends LLMException {

    public TimeoutException(String message) {
        super(message);
    }

    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
