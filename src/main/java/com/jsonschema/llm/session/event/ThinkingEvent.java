package com.jsonschema.llm.session.event;

public class ThinkingEvent extends Event {
    private final String thought;
    private final String reasoning;
    
    public ThinkingEvent(String sessionId, String thought, String reasoning) {
        super(EventType.THINKING, sessionId);
        this.thought = thought;
        this.reasoning = reasoning;
    }
    
    public String getThought() {
        return thought;
    }
    
    public String getReasoning() {
        return reasoning;
    }
    
    @Override
    public String toString() {
        return "ThinkingEvent{" +
                "id='" + getId() + '\'' +
                ", thought='" + thought + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
