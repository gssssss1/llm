package com.jsonschema.llm.message;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class AssistantMessage extends Message {
    private final List<ToolCall> toolCalls;
    
    public AssistantMessage(String content) {
        super(MessageRole.ASSISTANT, content);
        this.toolCalls = new ArrayList<>();
    }
    
    public AssistantMessage() {
        super(MessageRole.ASSISTANT);
        this.toolCalls = new ArrayList<>();
    }
    
    public List<ToolCall> getToolCalls() {
        return toolCalls;
    }
    
    public void addToolCall(ToolCall toolCall) {
        toolCalls.add(toolCall);
    }
    
    public void addToolCall(String id, String name, String arguments) {
        toolCalls.add(new ToolCall(id, name, arguments));
    }
    
    public boolean hasToolCalls() {
        return !toolCalls.isEmpty();
    }
    
    @Override
    public JsonObject toOpenAIFormat() {
        JsonObject json = new JsonObject();
        json.addProperty("role", role.getValue());
        
        if (content != null && !content.isEmpty()) {
            json.addProperty("content", content);
        } else {
            json.add("content", null);
        }
        
        if (!toolCalls.isEmpty()) {
            JsonArray toolCallsArray = new JsonArray();
            for (ToolCall toolCall : toolCalls) {
                toolCallsArray.add(toolCall.toOpenAIFormat());
            }
            json.add("tool_calls", toolCallsArray);
        }
        
        return json;
    }
    
    @Override
    public JsonObject toAnthropicFormat() {
        JsonObject json = new JsonObject();
        json.addProperty("role", role.getValue());
        
        if (content != null && !content.isEmpty()) {
            json.addProperty("content", content);
        } else if (!toolCalls.isEmpty()) {
            JsonArray contentArray = new JsonArray();
            for (ToolCall toolCall : toolCalls) {
                contentArray.add(toolCall.toAnthropicFormat());
            }
            json.add("content", contentArray);
        } else {
            json.addProperty("content", "");
        }
        
        return json;
    }
    
    public static AssistantMessage of(String content) {
        return new AssistantMessage(content);
    }
    
    public static AssistantMessage withToolCalls(ToolCall... toolCalls) {
        AssistantMessage message = new AssistantMessage();
        for (ToolCall toolCall : toolCalls) {
            message.addToolCall(toolCall);
        }
        return message;
    }
}
