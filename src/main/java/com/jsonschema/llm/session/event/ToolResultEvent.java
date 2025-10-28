package com.jsonschema.llm.session.event;

public class ToolResultEvent extends Event {
    private final String toolCallId;
    private final String toolName;
    private final String result;
    private final boolean success;
    private final String error;
    
    public ToolResultEvent(String sessionId, String toolCallId, String toolName, String result, boolean success, String error) {
        super(EventType.TOOL_RESULT, sessionId);
        this.toolCallId = toolCallId;
        this.toolName = toolName;
        this.result = result;
        this.success = success;
        this.error = error;
    }
    
    public static ToolResultEvent success(String sessionId, String toolCallId, String toolName, String result) {
        return new ToolResultEvent(sessionId, toolCallId, toolName, result, true, null);
    }
    
    public static ToolResultEvent failure(String sessionId, String toolCallId, String toolName, String error) {
        return new ToolResultEvent(sessionId, toolCallId, toolName, null, false, error);
    }
    
    public String getToolCallId() {
        return toolCallId;
    }
    
    public String getToolName() {
        return toolName;
    }
    
    public String getResult() {
        return result;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getError() {
        return error;
    }
    
    @Override
    public String toString() {
        return "ToolResultEvent{" +
                "id='" + getId() + '\'' +
                ", toolCallId='" + toolCallId + '\'' +
                ", toolName='" + toolName + '\'' +
                ", success=" + success +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
