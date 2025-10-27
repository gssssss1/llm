package com.llm.core.message;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * System message defining assistant behavior and constraints.
 */
public class SystemMessage extends AbstractMessage {

    private SystemMessage(String id, List<MultiModalContent> contents, Map<String, Object> metadata) {
        super(id, MessageType.SYSTEM, contents, metadata);
    }

    public static SystemMessage of(String text) {
        return new SystemMessage(null, Collections.singletonList(MultiModalContent.text(text)), Collections.emptyMap());
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for system messages.
     */
    public static class Builder extends MessageBuilder<SystemMessage, Builder> {

        @Override
        protected SystemMessage buildInternal(String id, List<MultiModalContent> contents, Map<String, Object> metadata) {
            return new SystemMessage(id, contents, metadata);
        }
    }
}
