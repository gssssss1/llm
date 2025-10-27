package com.llm.exception;

/**
 * Exception thrown when authentication fails.
 */
public class AuthenticationException extends ProviderException {

    public AuthenticationException(String message) {
        super(401, message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(401, message, cause);
    }
}
