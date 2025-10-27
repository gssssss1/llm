package com.llm.exception;

/**
 * Exception thrown when token limit is exceeded.
 */
public class TokenLimitExceededException extends LLMException {

    private final int currentTokens;
    private final int maxTokens;

    public TokenLimitExceededException(int currentTokens, int maxTokens) {
        super(String.format("Token limit exceeded: %d > %d", currentTokens, maxTokens));
        this.currentTokens = currentTokens;
        this.maxTokens = maxTokens;
    }

    public int getCurrentTokens() {
        return currentTokens;
    }

    public int getMaxTokens() {
        return maxTokens;
    }
}
