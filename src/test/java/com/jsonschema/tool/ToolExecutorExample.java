package com.jsonschema.tool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToolExecutorExample {
    
    public static void main(String[] args) throws Exception {
        ToolExecutor executor = new ToolExecutor();
        ExampleTools tools = new ExampleTools();
        
        executor.registerTool(tools);
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        
        System.out.println("=== Registered Tools ===");
        System.out.println("Total tools: " + executor.getToolCount());
        System.out.println("Tool names: " + executor.getToolNames());
        System.out.println();
        
        System.out.println("=== Tool Schemas (for LLM) ===");
        List<JsonObject> schemas = executor.getAllToolSchemas();
        for (JsonObject schema : schemas) {
            System.out.println(gson.toJson(schema));
            System.out.println();
        }
        
        System.out.println("=== Example 1: Get Weather ===");
        Map<String, Object> weatherParams = new HashMap<>();
        weatherParams.put("city", "Shanghai");
        weatherParams.put("country", "China");
        weatherParams.put("unit", "celsius");
        
        Object weatherResult = executor.executeTool("getWeather", weatherParams);
        System.out.println("Result: " + weatherResult);
        System.out.println();
        
        System.out.println("=== Example 2: Search ===");
        String searchJson = "{\"query\":\"Java programming\",\"limit\":3,\"offset\":0}";
        Object searchResult = executor.executeTool("search", searchJson);
        System.out.println("Result: " + gson.toJson(searchResult));
        System.out.println();
        
        System.out.println("=== Example 3: Get Current Time (No Parameters) ===");
        Object timeResult = executor.executeTool("getCurrentTime");
        System.out.println("Result: " + timeResult);
        System.out.println();
        
        System.out.println("=== Example 4: Calculate ===");
        Map<String, Object> calcParams = new HashMap<>();
        calcParams.put("a", 15);
        calcParams.put("b", 7);
        calcParams.put("operation", "multiply");
        
        Object calcResult = executor.executeTool("calculate", calcParams);
        System.out.println("15 * 7 = " + calcResult);
        System.out.println();
        
        System.out.println("=== Example 5: Create User ===");
        String userJson = "{\"id\":\"user_001\",\"name\":\"张三\",\"email\":\"zhangsan@example.com\",\"age\":28,\"active\":true}";
        Object userResult = executor.executeTool("createUser", userJson);
        System.out.println("Result: " + userResult);
        System.out.println();
        
        System.out.println("=== Example 6: Tool Definition Details ===");
        for (String toolName : executor.getToolNames()) {
            ToolDefinition def = executor.getToolDefinition(toolName);
            System.out.println(def);
        }
        
        System.out.println();
        System.out.println("=== Simulating LLM Function Call ===");
        System.out.println("Suppose LLM decides to call 'getWeather' with parameters:");
        String llmParams = "{\"city\":\"Tokyo\",\"unit\":\"celsius\"}";
        System.out.println(llmParams);
        System.out.println();
        System.out.println("Executing tool...");
        Object llmResult = executor.executeTool("getWeather", llmParams);
        System.out.println("Response to LLM: " + llmResult);
    }
}
