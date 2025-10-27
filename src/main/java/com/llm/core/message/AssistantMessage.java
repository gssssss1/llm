package com.llm.core.message;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Message from the assistant / model.
 */
public class AssistantMessage extends AbstractMessage {

    private final String finishReason;

    private AssistantMessage(String id, List<MultiModalContent> contents, Map<String, Object> metadata, String finishReason) {
        super(id, MessageType.ASSISTANT, contents, metadata);
        this.finishReason = finishReason;
    }

    public static AssistantMessage of(String text) {
        return new AssistantMessage(null, Collections.singletonList(MultiModalContent.text(text)), Collections.emptyMap(), null);
    }

    public static AssistantMessage of(String text, String finishReason) {
        return new AssistantMessage(null, Collections.singletonList(MultiModalContent.text(text)), Collections.emptyMap(), finishReason);
    }

    public String getFinishReason() {
        return finishReason;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for assistant messages.
     */
    public static class Builder extends MessageBuilder<AssistantMessage, Builder> {
        private String finishReason;

        public Builder finishReason(String finishReason) {
            this.finishReason = finishReason;
            return this;
        }

        @Override
        protected AssistantMessage buildInternal(String id, List<MultiModalContent> contents, Map<String, Object> metadata) {
            return new AssistantMessage(id, contents, metadata, finishReason);
        }
    }
}
