package com.llmframework.core.usage;

/**
 * Token usage estimation
 */
public record TokenEstimate(
    int promptTokens,
    Integer maxCompletionTokens
) {
    public int estimatedTotal() {
        return promptTokens + (maxCompletionTokens != null ? maxCompletionTokens : 0);
    }
}
