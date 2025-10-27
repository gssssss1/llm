package com.llm.core.session;

/**
 * State of an LLM session.
 */
public enum SessionState {
    /**
     * No request is currently running.
     */
    IDLE,

    /**
     * A request is currently being processed.
     */
    RUNNING,

    /**
     * Session has been closed and cannot be reused.
     */
    CLOSED
}
