package com.llm.provider.common;

import com.llm.core.message.AssistantMessage;
import com.llm.core.tool.ToolCall;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Provider-agnostic response representation.
 */
public class Response {

    private final AssistantMessage message;
    private final List<ToolCall> toolCalls;
    private final Usage usage;
    private final String finishReason;
    private final Map<String, Object> metadata;

    private Response(Builder builder) {
        this.message = Objects.requireNonNull(builder.message, "message");
        this.toolCalls = Collections.unmodifiableList(builder.toolCalls);
        this.usage = builder.usage;
        this.finishReason = builder.finishReason;
        this.metadata = builder.metadata != null ? Map.copyOf(builder.metadata) : Collections.emptyMap();
    }

    public AssistantMessage getMessage() {
        return message;
    }

    public List<ToolCall> getToolCalls() {
        return toolCalls;
    }

    public Usage getUsage() {
        return usage;
    }

    public String getFinishReason() {
        return finishReason;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Token usage information.
     */
    public static class Usage {
        private final int promptTokens;
        private final int completionTokens;
        private final int totalTokens;

        public Usage(int promptTokens, int completionTokens, int totalTokens) {
            this.promptTokens = promptTokens;
            this.completionTokens = completionTokens;
            this.totalTokens = totalTokens;
        }

        public int getPromptTokens() {
            return promptTokens;
        }

        public int getCompletionTokens() {
            return completionTokens;
        }

        public int getTotalTokens() {
            return totalTokens;
        }
    }

    public static class Builder {
        private AssistantMessage message;
        private List<ToolCall> toolCalls = Collections.emptyList();
        private Usage usage;
        private String finishReason;
        private Map<String, Object> metadata;

        public Builder message(AssistantMessage message) {
            this.message = message;
            return this;
        }

        public Builder toolCalls(List<ToolCall> toolCalls) {
            this.toolCalls = toolCalls;
            return this;
        }

        public Builder usage(Usage usage) {
            this.usage = usage;
            return this;
        }

        public Builder usage(int promptTokens, int completionTokens, int totalTokens) {
            this.usage = new Usage(promptTokens, completionTokens, totalTokens);
            return this;
        }

        public Builder finishReason(String finishReason) {
            this.finishReason = finishReason;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Response build() {
            return new Response(this);
        }
    }
}
