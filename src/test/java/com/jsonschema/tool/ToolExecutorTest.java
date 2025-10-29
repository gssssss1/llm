package com.jsonschema.tool;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ToolExecutorTest {
    
    private ToolExecutor executor;
    private ExampleTools exampleTools;
    
    @BeforeEach
    public void setUp() {
        executor = new ToolExecutor();
        exampleTools = new ExampleTools();
        executor.registerTool(exampleTools);
    }
    
    @Test
    public void testToolRegistration() {
        assertEquals(5, executor.getToolCount());
        assertTrue(executor.getToolNames().contains("getWeather"));
        assertTrue(executor.getToolNames().contains("search"));
        assertTrue(executor.getToolNames().contains("getCurrentTime"));
        assertTrue(executor.getToolNames().contains("createUser"));
        assertTrue(executor.getToolNames().contains("calculate"));
    }
    
    @Test
    public void testGetAllToolSchemas() {
        List<JsonObject> schemas = executor.getAllToolSchemas();
        assertEquals(5, schemas.size());
        
        for (JsonObject schema : schemas) {
            assertTrue(schema.has("name"));
            assertTrue(schema.has("description"));
            assertTrue(schema.has("parameters"));
        }
    }
    
    @Test
    public void testGetToolSchema() {
        JsonObject schema = executor.getToolSchema("getWeather");
        assertNotNull(schema);
        assertEquals("getWeather", schema.get("name").getAsString());
        assertTrue(schema.get("description").getAsString().contains("weather"));
        
        JsonObject parameters = schema.getAsJsonObject("parameters");
        assertNotNull(parameters);
        assertTrue(parameters.has("properties"));
    }
    
    @Test
    public void testExecuteWeatherTool() throws Exception {
        String json = "{\"city\":\"Beijing\",\"country\":\"China\",\"unit\":\"celsius\"}";
        Object result = executor.executeTool("getWeather", json);
        
        assertNotNull(result);
        assertTrue(result instanceof String);
        String weatherInfo = (String) result;
        assertTrue(weatherInfo.contains("Beijing"));
        assertTrue(weatherInfo.contains("China"));
    }
    
    @Test
    public void testExecuteSearchTool() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("query", "Java");
        params.put("limit", 5);
        params.put("offset", 0);
        
        Object result = executor.executeTool("search", params);
        
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> searchResult = (Map<String, Object>) result;
        assertEquals("Java", searchResult.get("query"));
        assertEquals(5, searchResult.get("limit"));
    }
    
    @Test
    public void testExecuteNoParameterTool() throws Exception {
        Object result = executor.executeTool("getCurrentTime");
        
        assertNotNull(result);
        assertTrue(result instanceof String);
        String timeInfo = (String) result;
        assertTrue(timeInfo.contains("Current time:"));
    }
    
    @Test
    public void testExecuteCalculateTool() throws Exception {
        String json = "{\"a\":10,\"b\":5,\"operation\":\"add\"}";
        Object result = executor.executeTool("calculate", json);
        
        assertNotNull(result);
        assertEquals(15.0, (Double) result, 0.001);
    }
    
    @Test
    public void testExecuteCalculateSubtract() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("a", 20);
        params.put("b", 8);
        params.put("operation", "subtract");
        
        Object result = executor.executeTool("calculate", params);
        assertEquals(12.0, (Double) result, 0.001);
    }
    
    @Test
    public void testExecuteCalculateMultiply() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("a", 6);
        params.put("b", 7);
        params.put("operation", "multiply");
        
        Object result = executor.executeTool("calculate", params);
        assertEquals(42.0, (Double) result, 0.001);
    }
    
    @Test
    public void testExecuteCalculateDivide() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("a", 100);
        params.put("b", 4);
        params.put("operation", "divide");
        
        Object result = executor.executeTool("calculate", params);
        assertEquals(25.0, (Double) result, 0.001);
    }
    
    @Test
    public void testToolNotFound() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            executor.executeTool("nonexistentTool", "{}");
        });
        
        assertTrue(exception.getMessage().contains("Tool not found"));
    }
    
    @Test
    public void testCreateUserTool() throws Exception {
        String json = "{\"id\":\"123\",\"name\":\"Alice\",\"email\":\"alice@example.com\",\"age\":25,\"active\":true}";
        Object result = executor.executeTool("createUser", json);
        
        assertNotNull(result);
        assertTrue(result instanceof String);
        String userInfo = (String) result;
        assertTrue(userInfo.contains("Alice"));
        assertTrue(userInfo.contains("alice@example.com"));
        assertTrue(userInfo.contains("25"));
    }
    
    @Test
    public void testGetToolDefinition() {
        ToolDefinition toolDef = executor.getToolDefinition("getWeather");
        
        assertNotNull(toolDef);
        assertEquals("getWeather", toolDef.getName());
        assertTrue(toolDef.getDescription().contains("weather"));
        assertNotNull(toolDef.getMethod());
        assertNotNull(toolDef.getParameterClass());
        assertEquals(WeatherRequest.class, toolDef.getParameterClass());
    }
}
