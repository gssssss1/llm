package com.llm.core.session;

import com.llm.core.message.Message;

import java.util.List;

/**
 * Represents history of messages in a session context.
 */
public interface MessageHistory {

    /**
     * Strategy for managing message history.
     */
    enum Strategy {
        FULL,
        SLIDING_WINDOW,
        TOKEN_LIMIT,
        SUMMARY
    }

    /**
     * Adds a message to the history.
     */
    void add(Message message);

    /**
     * Adds multiple messages.
     */
    void addAll(List<Message> messages);

    /**
     * Returns messages in the history.
     */
    List<Message> getMessages();

    /**
     * Clears the history.
     */
    void clear();

    /**
     * Rolls back the last n messages.
     */
    void rollback(int steps);

    /**
     * Creates a copy of the history.
     */
    MessageHistory copy();
}
