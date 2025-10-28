package com.jsonschema.llm.session.event;

public class AssistantMessageEvent extends Event {
    private final String content;
    private final String model;
    private final int tokensUsed;
    
    public AssistantMessageEvent(String sessionId, String content, String model, int tokensUsed) {
        super(EventType.ASSISTANT_MESSAGE, sessionId);
        this.content = content;
        this.model = model;
        this.tokensUsed = tokensUsed;
    }
    
    public String getContent() {
        return content;
    }
    
    public String getModel() {
        return model;
    }
    
    public int getTokensUsed() {
        return tokensUsed;
    }
    
    @Override
    public String toString() {
        return "AssistantMessageEvent{" +
                "id='" + getId() + '\'' +
                ", content='" + content + '\'' +
                ", model='" + model + '\'' +
                ", tokensUsed=" + tokensUsed +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
