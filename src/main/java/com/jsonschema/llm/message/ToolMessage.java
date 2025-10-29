package com.jsonschema.llm.message;

import com.google.gson.JsonObject;

public class ToolMessage extends Message {
    private final String toolCallId;
    private final String toolName;
    
    public ToolMessage(String toolCallId, String content) {
        super(MessageRole.TOOL, content);
        this.toolCallId = toolCallId;
        this.toolName = null;
    }
    
    public ToolMessage(String toolCallId, String toolName, String content) {
        super(MessageRole.TOOL, content);
        this.toolCallId = toolCallId;
        this.toolName = toolName;
    }
    
    public String getToolCallId() {
        return toolCallId;
    }
    
    public String getToolName() {
        return toolName;
    }
    
    @Override
    public JsonObject toOpenAIFormat() {
        JsonObject json = new JsonObject();
        json.addProperty("role", role.getValue());
        json.addProperty("content", content);
        json.addProperty("tool_call_id", toolCallId);
        return json;
    }
    
    @Override
    public JsonObject toAnthropicFormat() {
        JsonObject json = new JsonObject();
        json.addProperty("role", "user");
        
        JsonObject contentObj = new JsonObject();
        contentObj.addProperty("type", "tool_result");
        contentObj.addProperty("tool_use_id", toolCallId);
        contentObj.addProperty("content", content);
        
        json.add("content", contentObj);
        return json;
    }
    
    public static ToolMessage of(String toolCallId, String content) {
        return new ToolMessage(toolCallId, content);
    }
    
    public static ToolMessage of(String toolCallId, String toolName, String content) {
        return new ToolMessage(toolCallId, toolName, content);
    }
}
