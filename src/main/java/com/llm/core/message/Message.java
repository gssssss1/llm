package com.llm.core.message;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Represents a message exchanged within an LLM session.
 */
public interface Message {

    /**
     * Unique identifier of the message.
     *
     * @return message id
     */
    String getId();

    /**
     * Timestamp when the message was created.
     *
     * @return timestamp
     */
    Instant getTimestamp();

    /**
     * Type of message.
     *
     * @return message type
     */
    MessageType getType();

    /**
     * Content elements of the message.
     *
     * @return list of contents
     */
    List<MultiModalContent> getContents();

    /**
     * Arbitrary metadata for the message.
     *
     * @return metadata map
     */
    Map<String, Object> getMetadata();
}
