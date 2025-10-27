package com.llm.exception;

/**
 * Exception thrown when a provider-specific error occurs.
 */
public class ProviderException extends LLMException {

    private final int statusCode;

    public ProviderException(String message) {
        super(message);
        this.statusCode = 0;
    }

    public ProviderException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
    }

    public ProviderException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public ProviderException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
