package com.llm.core.session;

import com.llm.core.message.Message;
import com.llm.provider.common.Response;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Core interface representing an LLM conversation session.
 */
public interface LLMSession extends AutoCloseable {

    /**
     * Sends a message synchronously and returns the response.
     */
    Response send(Message message);

    /**
     * Sends multiple messages synchronously.
     */
    Response send(List<Message> messages);

    /**
     * Sends a message asynchronously.
     */
    CompletableFuture<Response> sendAsync(Message message);

    /**
     * Sends a message with streaming response.
     */
    void sendStream(Message message, Consumer<String> onChunk);

    /**
     * Sends a batch of messages and returns batch responses.
     */
    List<Response> sendBatch(List<Message> messages);

    /**
     * Returns current session state.
     */
    SessionState getState();

    /**
     * Returns session configuration.
     */
    SessionConfig getConfig();

    /**
     * Returns session context (history, variables, metadata).
     */
    SessionContext getContext();

    /**
     * Returns list of messages in the conversation history.
     */
    List<Message> getHistory();

    /**
     * Clears the conversation history.
     */
    void clearHistory();

    /**
     * Rolls back the last n messages.
     */
    void rollback(int steps);

    /**
     * Resets the session to initial state.
     */
    void reset();

    /**
     * Creates a fork of the current session.
     */
    LLMSession fork();

    /**
     * Saves the session state.
     */
    void save(String path);

    /**
     * Closes the session and releases resources.
     */
    @Override
    void close();
}
