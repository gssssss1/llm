package com.jsonschema.llm.message;

import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.List;

public class Conversation {
    private final List<Message> messages;
    
    public Conversation() {
        this.messages = new ArrayList<>();
    }
    
    public Conversation addMessage(Message message) {
        messages.add(message);
        return this;
    }
    
    public Conversation addSystem(String content) {
        messages.add(new SystemMessage(content));
        return this;
    }
    
    public Conversation addUser(String content) {
        messages.add(new UserMessage(content));
        return this;
    }
    
    public Conversation addAssistant(String content) {
        messages.add(new AssistantMessage(content));
        return this;
    }
    
    public Conversation addTool(String toolCallId, String content) {
        messages.add(new ToolMessage(toolCallId, content));
        return this;
    }
    
    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }
    
    public Message getLastMessage() {
        return messages.isEmpty() ? null : messages.get(messages.size() - 1);
    }
    
    public int size() {
        return messages.size();
    }
    
    public boolean isEmpty() {
        return messages.isEmpty();
    }
    
    public void clear() {
        messages.clear();
    }
    
    public JsonArray toOpenAIFormat() {
        JsonArray array = new JsonArray();
        for (Message message : messages) {
            array.add(message.toOpenAIFormat());
        }
        return array;
    }
    
    public JsonArray toAnthropicFormat() {
        JsonArray array = new JsonArray();
        for (Message message : messages) {
            array.add(message.toAnthropicFormat());
        }
        return array;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Conversation{\n");
        for (int i = 0; i < messages.size(); i++) {
            sb.append("  [").append(i).append("] ").append(messages.get(i)).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
