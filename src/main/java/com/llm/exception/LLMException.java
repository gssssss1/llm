package com.llm.exception;

/**
 * Base exception for all errors thrown by the LLM session framework.
 */
public class LLMException extends RuntimeException {

    private final String errorCode;

    public LLMException(String message) {
        super(message);
        this.errorCode = null;
    }

    public LLMException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    public LLMException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public LLMException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
