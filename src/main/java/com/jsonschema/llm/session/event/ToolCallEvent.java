package com.jsonschema.llm.session.event;

public class ToolCallEvent extends Event {
    private final String toolName;
    private final String toolCallId;
    private final String arguments;
    
    public ToolCallEvent(String sessionId, String toolName, String toolCallId, String arguments) {
        super(EventType.TOOL_CALL, sessionId);
        this.toolName = toolName;
        this.toolCallId = toolCallId;
        this.arguments = arguments;
    }
    
    public String getToolName() {
        return toolName;
    }
    
    public String getToolCallId() {
        return toolCallId;
    }
    
    public String getArguments() {
        return arguments;
    }
    
    @Override
    public String toString() {
        return "ToolCallEvent{" +
                "id='" + getId() + '\'' +
                ", toolName='" + toolName + '\'' +
                ", toolCallId='" + toolCallId + '\'' +
                ", arguments='" + arguments + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
