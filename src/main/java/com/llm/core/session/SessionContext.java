package com.llm.core.session;

import com.llm.core.message.Message;
import com.llm.core.message.SystemMessage;

import java.util.*;

/**
 * Session context holding message history, system prompt, variables and metadata.
 */
public class SessionContext {

    private final MessageHistory history;
    private SystemMessage systemPrompt;
    private final Map<String, Object> variables;
    private final Map<String, Object> metadata;

    private SessionContext(Builder builder) {
        this.history = builder.history != null ? builder.history : new FullMessageHistory();
        this.systemPrompt = builder.systemPrompt;
        this.variables = new HashMap<>(builder.variables);
        this.metadata = new HashMap<>(builder.metadata);
    }

    public MessageHistory getHistory() {
        return history;
    }

    public Optional<SystemMessage> getSystemPrompt() {
        return Optional.ofNullable(systemPrompt);
    }

    public void setSystemPrompt(SystemMessage systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }

    public Object getVariable(String key) {
        return variables.get(key);
    }

    public Map<String, Object> getVariables() {
        return Collections.unmodifiableMap(variables);
    }

    public void removeVariable(String key) {
        variables.remove(key);
    }

    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        return metadata.get(key);
    }

    public Map<String, Object> getAllMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Creates a snapshot of the current context.
     */
    public ContextSnapshot createSnapshot() {
        return new ContextSnapshot(
                history.copy(),
                systemPrompt,
                new HashMap<>(variables),
                new HashMap<>(metadata)
        );
    }

    /**
     * Restores context from a snapshot.
     */
    public void restoreFromSnapshot(ContextSnapshot snapshot) {
        history.clear();
        history.addAll(snapshot.getMessages());
        this.systemPrompt = snapshot.getSystemPrompt();
        this.variables.clear();
        this.variables.putAll(snapshot.getVariables());
        this.metadata.clear();
        this.metadata.putAll(snapshot.getMetadata());
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for session context.
     */
    public static class Builder {
        private MessageHistory history;
        private SystemMessage systemPrompt;
        private Map<String, Object> variables = new HashMap<>();
        private Map<String, Object> metadata = new HashMap<>();

        public Builder history(MessageHistory history) {
            this.history = history;
            return this;
        }

        public Builder systemPrompt(String prompt) {
            this.systemPrompt = SystemMessage.of(prompt);
            return this;
        }

        public Builder systemPrompt(SystemMessage systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        public Builder variable(String key, Object value) {
            variables.put(key, value);
            return this;
        }

        public Builder metadata(String key, Object value) {
            metadata.put(key, value);
            return this;
        }

        public SessionContext build() {
            return new SessionContext(this);
        }
    }

    /**
     * Snapshot of the context at a point in time.
     */
    public static class ContextSnapshot {
        private final List<Message> messages;
        private final SystemMessage systemPrompt;
        private final Map<String, Object> variables;
        private final Map<String, Object> metadata;

        public ContextSnapshot(MessageHistory history, SystemMessage systemPrompt, 
                             Map<String, Object> variables, Map<String, Object> metadata) {
            this.messages = history.getMessages();
            this.systemPrompt = systemPrompt;
            this.variables = new HashMap<>(variables);
            this.metadata = new HashMap<>(metadata);
        }

        public List<Message> getMessages() {
            return messages;
        }

        public SystemMessage getSystemPrompt() {
            return systemPrompt;
        }

        public Map<String, Object> getVariables() {
            return variables;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }
    }
}
