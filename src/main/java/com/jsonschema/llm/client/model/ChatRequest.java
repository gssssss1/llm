package com.jsonschema.llm.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jsonschema.llm.message.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRequest {
    private String model;
    private List<Message> messages;
    private List<JsonObject> tools;
    private Double temperature;
    private Integer maxTokens;
    private Double topP;
    private Integer n;
    private Boolean stream;
    private List<String> stop;
    private Map<String, Object> additionalProperties;
    
    public ChatRequest() {
        this.messages = new ArrayList<>();
        this.tools = new ArrayList<>();
        this.additionalProperties = new HashMap<>();
    }
    
    public ChatRequest(String model) {
        this();
        this.model = model;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public List<Message> getMessages() {
        return messages;
    }
    
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
    
    public void addMessage(Message message) {
        this.messages.add(message);
    }
    
    public List<JsonObject> getTools() {
        return tools;
    }
    
    public void setTools(List<JsonObject> tools) {
        this.tools = tools;
    }
    
    public void addTool(JsonObject tool) {
        this.tools.add(tool);
    }
    
    public Double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
    
    public Integer getMaxTokens() {
        return maxTokens;
    }
    
    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }
    
    public Double getTopP() {
        return topP;
    }
    
    public void setTopP(Double topP) {
        this.topP = topP;
    }
    
    public Integer getN() {
        return n;
    }
    
    public void setN(Integer n) {
        this.n = n;
    }
    
    public Boolean getStream() {
        return stream;
    }
    
    public void setStream(Boolean stream) {
        this.stream = stream;
    }
    
    public List<String> getStop() {
        return stop;
    }
    
    public void setStop(List<String> stop) {
        this.stop = stop;
    }
    
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }
    
    public void setAdditionalProperty(String key, Object value) {
        additionalProperties.put(key, value);
    }
    
    public JsonObject toOpenAIJson() {
        JsonObject json = new JsonObject();
        json.addProperty("model", model);
        
        JsonArray messagesArray = new JsonArray();
        for (Message message : messages) {
            messagesArray.add(message.toOpenAIFormat());
        }
        json.add("messages", messagesArray);
        
        if (tools != null && !tools.isEmpty()) {
            JsonArray toolsArray = new JsonArray();
            for (JsonObject tool : tools) {
                toolsArray.add(tool);
            }
            json.add("tools", toolsArray);
        }
        
        if (temperature != null) {
            json.addProperty("temperature", temperature);
        }
        
        if (maxTokens != null) {
            json.addProperty("max_tokens", maxTokens);
        }
        
        if (topP != null) {
            json.addProperty("top_p", topP);
        }
        
        if (n != null) {
            json.addProperty("n", n);
        }
        
        if (stream != null) {
            json.addProperty("stream", stream);
        }
        
        if (stop != null && !stop.isEmpty()) {
            JsonArray stopArray = new JsonArray();
            for (String s : stop) {
                stopArray.add(s);
            }
            json.add("stop", stopArray);
        }
        
        for (Map.Entry<String, Object> entry : additionalProperties.entrySet()) {
            json.addProperty(entry.getKey(), entry.getValue().toString());
        }
        
        return json;
    }
    
    public JsonObject toAnthropicJson() {
        JsonObject json = new JsonObject();
        json.addProperty("model", model);
        
        JsonArray messagesArray = new JsonArray();
        for (Message message : messages) {
            messagesArray.add(message.toAnthropicFormat());
        }
        json.add("messages", messagesArray);
        
        if (maxTokens != null) {
            json.addProperty("max_tokens", maxTokens);
        } else {
            json.addProperty("max_tokens", 1024);
        }
        
        if (tools != null && !tools.isEmpty()) {
            JsonArray toolsArray = new JsonArray();
            for (JsonObject tool : tools) {
                JsonObject claudeTool = new JsonObject();
                claudeTool.addProperty("name", tool.get("name").getAsString());
                claudeTool.addProperty("description", tool.get("description").getAsString());
                claudeTool.add("input_schema", tool.getAsJsonObject("parameters"));
                toolsArray.add(claudeTool);
            }
            json.add("tools", toolsArray);
        }
        
        if (temperature != null) {
            json.addProperty("temperature", temperature);
        }
        
        if (topP != null) {
            json.addProperty("top_p", topP);
        }
        
        if (stop != null && !stop.isEmpty()) {
            JsonArray stopArray = new JsonArray();
            for (String s : stop) {
                stopArray.add(s);
            }
            json.add("stop_sequences", stopArray);
        }
        
        return json;
    }
}
