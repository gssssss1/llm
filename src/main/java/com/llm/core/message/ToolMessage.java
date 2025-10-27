package com.llm.core.message;

import java.util.*;

/**
 * Message representing tool/function call results.
 */
public class ToolMessage extends AbstractMessage {

    private final String toolCallId;
    private final String toolName;

    private ToolMessage(String id, List<MultiModalContent> contents, Map<String, Object> metadata, 
                       String toolCallId, String toolName) {
        super(id, MessageType.TOOL, contents, metadata);
        this.toolCallId = toolCallId;
        this.toolName = toolName;
    }

    public static ToolMessage of(String toolCallId, String toolName, String result) {
        return new ToolMessage(null, Collections.singletonList(MultiModalContent.text(result)), 
                              Collections.emptyMap(), toolCallId, toolName);
    }

    public String getToolCallId() {
        return toolCallId;
    }

    public String getToolName() {
        return toolName;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for tool messages.
     */
    public static class Builder extends MessageBuilder<ToolMessage, Builder> {
        private String toolCallId;
        private String toolName;

        public Builder toolCallId(String toolCallId) {
            this.toolCallId = toolCallId;
            return this;
        }

        public Builder toolName(String toolName) {
            this.toolName = toolName;
            return this;
        }

        @Override
        protected ToolMessage buildInternal(String id, List<MultiModalContent> contents, Map<String, Object> metadata) {
            return new ToolMessage(id, contents, metadata, toolCallId, toolName);
        }
    }
}
