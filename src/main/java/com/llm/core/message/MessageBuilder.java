package com.llm.core.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fluent builder API for constructing messages.
 *
 * @param <T> message type
 * @param <B> builder type
 */
public abstract class MessageBuilder<T extends Message, B extends MessageBuilder<T, B>> {
    private String id;
    private final List<MultiModalContent> contents = new ArrayList<>();
    private final Map<String, Object> metadata = new HashMap<>();

    protected MessageBuilder() {
    }

    protected abstract T buildInternal(String id, List<MultiModalContent> contents, Map<String, Object> metadata);

    public B id(String id) {
        this.id = id;
        return self();
    }

    public B addContent(MultiModalContent content) {
        this.contents.add(content);
        return self();
    }

    public B text(String text) {
        return addContent(MultiModalContent.text(text));
    }

    public B metadata(String key, Object value) {
        metadata.put(key, value);
        return self();
    }

    public T build() {
        return buildInternal(id, contents, metadata);
    }

    @SuppressWarnings("unchecked")
    private B self() {
        return (B) this;
    }
}
