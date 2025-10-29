package com.jsonschema.llm.session.event;

import java.time.Instant;
import java.util.UUID;

public abstract class Event {
    private final String id;
    private final EventType type;
    private final Instant timestamp;
    private final String sessionId;
    
    protected Event(EventType type, String sessionId) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.timestamp = Instant.now();
        this.sessionId = sessionId;
    }
    
    public String getId() {
        return id;
    }
    
    public EventType getType() {
        return type;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    @Override
    public String toString() {
        return "Event{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", timestamp=" + timestamp +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}
