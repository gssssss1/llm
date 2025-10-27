package com.llm.core.message;

/**
 * Enumeration of message types.
 */
public enum MessageType {
    /**
     * System message defining assistant behavior.
     */
    SYSTEM,

    /**
     * User message.
     */
    USER,

    /**
     * Assistant message.
     */
    ASSISTANT,

    /**
     * Tool/function call result message.
     */
    TOOL
}
