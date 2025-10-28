package com.jsonschema.llm.message;

public class MessageBuilder {
    
    public static SystemMessage system(String content) {
        return new SystemMessage(content);
    }
    
    public static UserMessage user(String content) {
        return new UserMessage(content);
    }
    
    public static AssistantMessage assistant(String content) {
        return new AssistantMessage(content);
    }
    
    public static AssistantMessage assistant() {
        return new AssistantMessage();
    }
    
    public static ToolMessage tool(String toolCallId, String content) {
        return new ToolMessage(toolCallId, content);
    }
    
    public static ToolMessage tool(String toolCallId, String toolName, String content) {
        return new ToolMessage(toolCallId, toolName, content);
    }
}
