package com.jsonschema.llm.message;

import com.google.gson.JsonObject;

public class ToolCall {
    private final String id;
    private final String name;
    private final String arguments;
    private final String type;
    
    public ToolCall(String id, String name, String arguments) {
        this.id = id;
        this.name = name;
        this.arguments = arguments;
        this.type = "function";
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getArguments() {
        return arguments;
    }
    
    public String getType() {
        return type;
    }
    
    public JsonObject toOpenAIFormat() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("type", type);
        
        JsonObject function = new JsonObject();
        function.addProperty("name", name);
        function.addProperty("arguments", arguments);
        
        json.add("function", function);
        return json;
    }
    
    public JsonObject toAnthropicFormat() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "tool_use");
        json.addProperty("id", id);
        json.addProperty("name", name);
        json.addProperty("input", arguments);
        return json;
    }
    
    @Override
    public String toString() {
        return "ToolCall{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", arguments='" + arguments + '\'' +
                '}';
    }
}
