package com.jsonschema.llm.message;

import com.google.gson.JsonObject;

public class SystemMessage extends Message {
    
    public SystemMessage(String content) {
        super(MessageRole.SYSTEM, content);
    }
    
    @Override
    public JsonObject toOpenAIFormat() {
        JsonObject json = new JsonObject();
        json.addProperty("role", role.getValue());
        json.addProperty("content", content);
        return json;
    }
    
    @Override
    public JsonObject toAnthropicFormat() {
        JsonObject json = new JsonObject();
        json.addProperty("role", role.getValue());
        json.addProperty("content", content);
        return json;
    }
    
    public static SystemMessage of(String content) {
        return new SystemMessage(content);
    }
}
