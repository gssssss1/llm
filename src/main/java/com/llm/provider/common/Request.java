package com.llm.provider.common;

import com.llm.core.message.Message;
import com.llm.core.session.SessionConfig;
import com.llm.core.session.SessionContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Provider-agnostic request representation.
 */
public class Request {

    private final List<Message> messages;
    private final SessionConfig config;
    private final SessionContext context;
    private final boolean streaming;

    private Request(Builder builder) {
        this.messages = Collections.unmodifiableList(new ArrayList<>(builder.messages));
        this.config = Objects.requireNonNull(builder.config, "config");
        this.context = Objects.requireNonNull(builder.context, "context");
        this.streaming = builder.streaming;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public SessionConfig getConfig() {
        return config;
    }

    public SessionContext getContext() {
        return context;
    }

    public boolean isStreaming() {
        return streaming;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<Message> messages = new ArrayList<>();
        private SessionConfig config;
        private SessionContext context;
        private boolean streaming;

        public Builder addMessage(Message message) {
            this.messages.add(message);
            return this;
        }

        public Builder messages(List<Message> messages) {
            this.messages.addAll(messages);
            return this;
        }

        public Builder config(SessionConfig config) {
            this.config = config;
            return this;
        }

        public Builder context(SessionContext context) {
            this.context = context;
            return this;
        }

        public Builder streaming(boolean streaming) {
            this.streaming = streaming;
            return this;
        }

        public Request build() {
            return new Request(this);
        }
    }
}
