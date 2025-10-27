package com.llm.persistence;

import com.llm.core.message.*;
import com.llm.core.session.SessionConfig;
import com.llm.core.session.SessionContext;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Snapshot of a session state for persistence and restoration.
 */
public class SessionSnapshot {

    private final String sessionId;
    private final SessionConfig config;
    private final List<MessageRecord> history;
    private final Map<String, Object> variables;
    private final Map<String, Object> metadata;
    private final Instant createdAt;

    public SessionSnapshot(String sessionId, SessionConfig config, List<MessageRecord> history,
                           Map<String, Object> variables, Map<String, Object> metadata, Instant createdAt) {
        this.sessionId = sessionId;
        this.config = config;
        this.history = history;
        this.variables = variables;
        this.metadata = metadata;
        this.createdAt = createdAt;
    }

    public static SessionSnapshot from(String sessionId, SessionConfig config, SessionContext context) {
        List<MessageRecord> records = context.getHistory().getMessages().stream()
                .map(MessageRecord::from)
                .collect(Collectors.toList());
        return new SessionSnapshot(sessionId != null ? sessionId : UUID.randomUUID().toString(),
                config,
                records,
                context.getVariables(),
                context.getAllMetadata(),
                Instant.now());
    }

    public String getSessionId() {
        return sessionId;
    }

    public SessionConfig getConfig() {
        return config;
    }

    public List<MessageRecord> getHistory() {
        return history;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Converts the snapshot back into live messages.
     */
    public List<Message> toMessages() {
        List<Message> messages = new ArrayList<>();
        for (MessageRecord record : history) {
            messages.add(record.toMessage());
        }
        return messages;
    }

    /**
     * Snapshot representation of a message.
     */
    public static class MessageRecord {
        private final MessageType type;
        private final String text;
        private final String toolName;
        private final String toolCallId;
        private final Map<String, Object> metadata;

        public MessageRecord(MessageType type, String text, String toolName, String toolCallId, Map<String, Object> metadata) {
            this.type = Objects.requireNonNull(type, "type");
            this.text = text;
            this.toolName = toolName;
            this.toolCallId = toolCallId;
            this.metadata = metadata;
        }

        public static MessageRecord from(Message message) {
            String text = message instanceof AbstractMessage ? ((AbstractMessage) message).getTextContent() : "";
            String toolName = null;
            String toolCallId = null;
            if (message instanceof ToolMessage) {
                ToolMessage toolMessage = (ToolMessage) message;
                toolName = toolMessage.getToolName();
                toolCallId = toolMessage.getToolCallId();
            }
            return new MessageRecord(message.getType(), text, toolName, toolCallId, message.getMetadata());
        }

        public MessageType getType() {
            return type;
        }

        public String getText() {
            return text;
        }

        public String getToolName() {
            return toolName;
        }

        public String getToolCallId() {
            return toolCallId;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public Message toMessage() {
            switch (type) {
                case SYSTEM:
                    return SystemMessage.builder().text(text).build();
                case USER:
                    return UserMessage.builder().text(text).build();
                case TOOL:
                    return ToolMessage.builder().text(text).toolName(toolName).toolCallId(toolCallId).build();
                case ASSISTANT:
                default:
                    return AssistantMessage.builder().text(text).build();
            }
        }
    }
}
