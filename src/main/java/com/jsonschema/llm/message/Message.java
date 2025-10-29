package com.jsonschema.llm.message;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public abstract class Message {
    protected final MessageRole role;
    protected String content;
    protected final Map<String, Object> metadata;
    
    protected Message(MessageRole role) {
        this.role = role;
        this.metadata = new HashMap<>();
    }
    
    protected Message(MessageRole role, String content) {
        this.role = role;
        this.content = content;
        this.metadata = new HashMap<>();
    }
    
    public MessageRole getRole() {
        return role;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    public abstract JsonObject toOpenAIFormat();
    
    public abstract JsonObject toAnthropicFormat();
    
    @Override
    public String toString() {
        return "Message{" +
                "role=" + role +
                ", content='" + content + '\'' +
                '}';
    }
}
