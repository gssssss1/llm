package com.llm.core.message;

import java.time.Instant;
import java.util.*;

/**
 * Base implementation of the Message interface.
 */
public abstract class AbstractMessage implements Message {

    protected final String id;
    protected final Instant timestamp;
    protected final MessageType type;
    protected final List<MultiModalContent> contents;
    protected final Map<String, Object> metadata;

    protected AbstractMessage(String id, MessageType type, List<MultiModalContent> contents, Map<String, Object> metadata) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.type = Objects.requireNonNull(type, "type");
        this.contents = Collections.unmodifiableList(new ArrayList<>(contents));
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    @Override
    public List<MultiModalContent> getContents() {
        return contents;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Get text content from the message (first text content).
     */
    public String getTextContent() {
        return contents.stream()
                .filter(c -> c.getType() == MultiModalContent.Type.TEXT)
                .findFirst()
                .flatMap(MultiModalContent::getText)
                .orElse("");
    }
}
