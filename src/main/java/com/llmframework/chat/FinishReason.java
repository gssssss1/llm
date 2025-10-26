package com.llmframework.chat;

/**
 * Completion finish reason
 */
public enum FinishReason {
    STOP,           // Normal completion
    LENGTH,         // Max length reached
    TOOL_CALLS,     // Tool calls required
    CONTENT_FILTER, // Content filtered
    ERROR           // Error occurred
}
