package com.llmframework.core.usage;

/**
 * Token usage statistics
 */
public record Usage(
    int promptTokens,
    int completionTokens,
    int totalTokens
) {
    public static Usage of(int prompt, int completion) {
        return new Usage(prompt, completion, prompt + completion);
    }
}
