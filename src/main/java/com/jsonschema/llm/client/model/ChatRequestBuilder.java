package com.jsonschema.llm.client.model;

import com.google.gson.JsonObject;
import com.jsonschema.llm.message.Conversation;
import com.jsonschema.llm.message.Message;
import com.jsonschema.tool.ToolExecutor;

import java.util.Arrays;
import java.util.List;

public class ChatRequestBuilder {
    private final ChatRequest request;
    
    private ChatRequestBuilder() {
        this.request = new ChatRequest();
    }
    
    public static ChatRequestBuilder create() {
        return new ChatRequestBuilder();
    }
    
    public static ChatRequestBuilder create(String model) {
        ChatRequestBuilder builder = new ChatRequestBuilder();
        builder.request.setModel(model);
        return builder;
    }
    
    public ChatRequestBuilder model(String model) {
        request.setModel(model);
        return this;
    }
    
    public ChatRequestBuilder message(Message message) {
        request.addMessage(message);
        return this;
    }
    
    public ChatRequestBuilder messages(List<Message> messages) {
        request.setMessages(messages);
        return this;
    }
    
    public ChatRequestBuilder messages(Message... messages) {
        request.getMessages().addAll(Arrays.asList(messages));
        return this;
    }
    
    public ChatRequestBuilder conversation(Conversation conversation) {
        request.setMessages(conversation.getMessages());
        return this;
    }
    
    public ChatRequestBuilder tool(JsonObject tool) {
        request.addTool(tool);
        return this;
    }
    
    public ChatRequestBuilder tools(List<JsonObject> tools) {
        request.setTools(tools);
        return this;
    }
    
    public ChatRequestBuilder tools(ToolExecutor toolExecutor) {
        request.setTools(toolExecutor.getAllToolSchemas());
        return this;
    }
    
    public ChatRequestBuilder temperature(double temperature) {
        request.setTemperature(temperature);
        return this;
    }
    
    public ChatRequestBuilder maxTokens(int maxTokens) {
        request.setMaxTokens(maxTokens);
        return this;
    }
    
    public ChatRequestBuilder topP(double topP) {
        request.setTopP(topP);
        return this;
    }
    
    public ChatRequestBuilder n(int n) {
        request.setN(n);
        return this;
    }
    
    public ChatRequestBuilder stream(boolean stream) {
        request.setStream(stream);
        return this;
    }
    
    public ChatRequestBuilder stop(String... stop) {
        request.setStop(Arrays.asList(stop));
        return this;
    }
    
    public ChatRequestBuilder additionalProperty(String key, Object value) {
        request.setAdditionalProperty(key, value);
        return this;
    }
    
    public ChatRequest build() {
        if (request.getModel() == null || request.getModel().isEmpty()) {
            throw new IllegalStateException("Model is required");
        }
        if (request.getMessages().isEmpty()) {
            throw new IllegalStateException("At least one message is required");
        }
        return request;
    }
}
