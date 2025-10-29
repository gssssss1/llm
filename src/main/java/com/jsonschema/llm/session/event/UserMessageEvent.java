package com.jsonschema.llm.session.event;

public class UserMessageEvent extends Event {
    private final String content;
    private final String userId;
    
    public UserMessageEvent(String sessionId, String content, String userId) {
        super(EventType.USER_MESSAGE, sessionId);
        this.content = content;
        this.userId = userId;
    }
    
    public String getContent() {
        return content;
    }
    
    public String getUserId() {
        return userId;
    }
    
    @Override
    public String toString() {
        return "UserMessageEvent{" +
                "id='" + getId() + '\'' +
                ", content='" + content + '\'' +
                ", userId='" + userId + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
