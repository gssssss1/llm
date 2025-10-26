package com.llmframework.core.usage;

/**
 * Simple token counter utility
 * This is a simplified implementation. In production, use a proper tokenizer library.
 */
public class TokenCounter {
    
    /**
     * Estimate token count for text
     * Rough approximation: ~4 characters per token for English
     */
    public static int estimate(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return (int) Math.ceil(text.length() / 4.0);
    }
}
