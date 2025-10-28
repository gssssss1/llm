package com.jsonschema.llm.message;

import com.google.gson.JsonObject;

public class UserMessage extends Message {
    
    public UserMessage(String content) {
        super(MessageRole.USER, content);
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
    
    public static UserMessage of(String content) {
        return new UserMessage(content);
    }
}
