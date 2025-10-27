package com.llm.core.tool;

import java.util.Collections;
import java.util.Map;

/**
 * Result of executing a tool.
 */
public class ToolResult {

    private final String content;
    private final Map<String, Object> metadata;
    private final boolean success;

    private ToolResult(Builder builder) {
        this.content = builder.content;
        this.metadata = builder.metadata != null ? Collections.unmodifiableMap(builder.metadata) : Collections.emptyMap();
        this.success = builder.success;
    }

    public String getContent() {
        return content;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public boolean isSuccess() {
        return success;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String content;
        private Map<String, Object> metadata;
        private boolean success = true;

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public ToolResult build() {
            return new ToolResult(this);
        }
    }
}
