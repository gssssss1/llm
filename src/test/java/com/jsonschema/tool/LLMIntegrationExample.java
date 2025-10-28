package com.jsonschema.tool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class LLMIntegrationExample {
    
    public static void main(String[] args) throws Exception {
        ToolExecutor executor = new ToolExecutor();
        ExampleTools tools = new ExampleTools();
        executor.registerTool(tools);
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        
        System.out.println("=== OpenAI Function Calling Integration Example ===");
        System.out.println();
        
        JsonArray toolsArray = new JsonArray();
        for (JsonObject schema : executor.getAllToolSchemas()) {
            toolsArray.add(schema);
        }
        
        JsonObject chatRequest = new JsonObject();
        chatRequest.addProperty("model", "gpt-4");
        
        JsonArray messages = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", "What's the weather like in Paris?");
        messages.add(userMessage);
        
        chatRequest.add("messages", messages);
        chatRequest.add("tools", toolsArray);
        
        System.out.println("=== Request to OpenAI API ===");
        System.out.println(gson.toJson(chatRequest));
        System.out.println();
        
        System.out.println("=== Simulated OpenAI Response ===");
        JsonObject simulatedResponse = createSimulatedResponse();
        System.out.println(gson.toJson(simulatedResponse));
        System.out.println();
        
        System.out.println("=== Processing Tool Call ===");
        JsonObject toolCall = simulatedResponse
            .getAsJsonArray("choices")
            .get(0).getAsJsonObject()
            .getAsJsonObject("message")
            .getAsJsonArray("tool_calls")
            .get(0).getAsJsonObject();
        
        String toolName = toolCall.getAsJsonObject("function").get("name").getAsString();
        String arguments = toolCall.getAsJsonObject("function").get("arguments").getAsString();
        
        System.out.println("Tool: " + toolName);
        System.out.println("Arguments: " + arguments);
        System.out.println();
        
        System.out.println("=== Executing Tool ===");
        Object result = executor.executeTool(toolName, arguments);
        System.out.println("Result: " + result);
        System.out.println();
        
        System.out.println("=== Sending Result Back to LLM ===");
        JsonObject toolMessage = new JsonObject();
        toolMessage.addProperty("role", "tool");
        toolMessage.addProperty("tool_call_id", toolCall.get("id").getAsString());
        toolMessage.addProperty("content", result.toString());
        
        messages.add(simulatedResponse
            .getAsJsonArray("choices")
            .get(0).getAsJsonObject()
            .getAsJsonObject("message"));
        messages.add(toolMessage);
        
        chatRequest.add("messages", messages);
        
        System.out.println("Final request with tool result:");
        System.out.println(gson.toJson(chatRequest));
        System.out.println();
        
        System.out.println("=== Anthropic Claude Tool Use Format ===");
        JsonObject claudeRequest = createClaudeRequest(executor);
        System.out.println(gson.toJson(claudeRequest));
    }
    
    private static JsonObject createSimulatedResponse() {
        JsonObject response = new JsonObject();
        response.addProperty("id", "chatcmpl-123");
        response.addProperty("object", "chat.completion");
        response.addProperty("created", System.currentTimeMillis() / 1000);
        response.addProperty("model", "gpt-4");
        
        JsonArray choices = new JsonArray();
        JsonObject choice = new JsonObject();
        choice.addProperty("index", 0);
        
        JsonObject message = new JsonObject();
        message.addProperty("role", "assistant");
        message.add("content", null);
        
        JsonArray toolCalls = new JsonArray();
        JsonObject toolCall = new JsonObject();
        toolCall.addProperty("id", "call_abc123");
        toolCall.addProperty("type", "function");
        
        JsonObject function = new JsonObject();
        function.addProperty("name", "getWeather");
        function.addProperty("arguments", "{\"city\":\"Paris\",\"country\":\"France\",\"unit\":\"celsius\"}");
        
        toolCall.add("function", function);
        toolCalls.add(toolCall);
        
        message.add("tool_calls", toolCalls);
        choice.add("message", message);
        choice.addProperty("finish_reason", "tool_calls");
        
        choices.add(choice);
        response.add("choices", choices);
        
        return response;
    }
    
    private static JsonObject createClaudeRequest(ToolExecutor executor) {
        JsonObject request = new JsonObject();
        request.addProperty("model", "claude-3-opus-20240229");
        request.addProperty("max_tokens", 1024);
        
        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", "What's the weather in London?");
        messages.add(message);
        request.add("messages", messages);
        
        JsonArray tools = new JsonArray();
        for (JsonObject schema : executor.getAllToolSchemas()) {
            JsonObject tool = new JsonObject();
            tool.addProperty("name", schema.get("name").getAsString());
            tool.addProperty("description", schema.get("description").getAsString());
            tool.add("input_schema", schema.getAsJsonObject("parameters"));
            tools.add(tool);
        }
        request.add("tools", tools);
        
        return request;
    }
}
