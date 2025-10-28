package com.jsonschema.tool;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jsonschema.annotations.Tool;
import com.jsonschema.generator.JsonSchemaGenerator;

import java.lang.reflect.Method;
import java.util.*;

public class ToolExecutor {
    
    private final Map<String, ToolDefinition> tools = new LinkedHashMap<>();
    private final JsonSchemaGenerator schemaGenerator = new JsonSchemaGenerator();
    private final Gson gson = new Gson();
    
    public void registerTool(Object toolProvider) {
        Class<?> clazz = toolProvider.getClass();
        
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Tool.class)) {
                Tool toolAnnotation = method.getAnnotation(Tool.class);
                
                String toolName = toolAnnotation.name().isEmpty() 
                    ? method.getName() 
                    : toolAnnotation.name();
                String description = toolAnnotation.description();
                
                Class<?> parameterClass = null;
                JsonObject schema = null;
                
                if (method.getParameterCount() > 0) {
                    parameterClass = method.getParameterTypes()[0];
                    
                    if (!isPrimitiveOrString(parameterClass)) {
                        schema = schemaGenerator.generateSchemaForFunctionCalling(
                            parameterClass,
                            toolName,
                            description
                        );
                    } else {
                        schema = createSimpleSchema(toolName, description, parameterClass);
                    }
                } else {
                    schema = createEmptySchema(toolName, description);
                }
                
                ToolDefinition toolDef = new ToolDefinition(
                    toolName,
                    description,
                    method,
                    toolProvider,
                    parameterClass,
                    schema
                );
                
                tools.put(toolName, toolDef);
            }
        }
    }
    
    public void registerTools(Object... toolProviders) {
        for (Object provider : toolProviders) {
            registerTool(provider);
        }
    }
    
    public Object executeTool(String toolName, String jsonParameters) throws Exception {
        ToolDefinition tool = tools.get(toolName);
        if (tool == null) {
            throw new IllegalArgumentException("Tool not found: " + toolName);
        }
        
        Method method = tool.getMethod();
        Object instance = tool.getInstance();
        
        method.setAccessible(true);
        
        if (method.getParameterCount() == 0) {
            return method.invoke(instance);
        }
        
        Class<?> paramClass = tool.getParameterClass();
        Object parameter = gson.fromJson(jsonParameters, paramClass);
        
        return method.invoke(instance, parameter);
    }
    
    public Object executeTool(String toolName, JsonObject jsonParameters) throws Exception {
        return executeTool(toolName, jsonParameters.toString());
    }
    
    public Object executeTool(String toolName, Map<String, Object> parameters) throws Exception {
        String json = gson.toJson(parameters);
        return executeTool(toolName, json);
    }
    
    public Object executeTool(String toolName) throws Exception {
        return executeTool(toolName, "{}");
    }
    
    public List<JsonObject> getAllToolSchemas() {
        List<JsonObject> schemas = new ArrayList<>();
        for (ToolDefinition tool : tools.values()) {
            schemas.add(tool.getSchema());
        }
        return schemas;
    }
    
    public JsonObject getToolSchema(String toolName) {
        ToolDefinition tool = tools.get(toolName);
        return tool != null ? tool.getSchema() : null;
    }
    
    public Set<String> getToolNames() {
        return tools.keySet();
    }
    
    public ToolDefinition getToolDefinition(String toolName) {
        return tools.get(toolName);
    }
    
    public Collection<ToolDefinition> getAllToolDefinitions() {
        return tools.values();
    }
    
    public int getToolCount() {
        return tools.size();
    }
    
    private boolean isPrimitiveOrString(Class<?> clazz) {
        return clazz.isPrimitive() || 
               clazz == String.class || 
               clazz == Integer.class || 
               clazz == Long.class || 
               clazz == Double.class || 
               clazz == Float.class || 
               clazz == Boolean.class;
    }
    
    private JsonObject createSimpleSchema(String name, String description, Class<?> paramClass) {
        JsonObject schema = new JsonObject();
        schema.addProperty("name", name);
        schema.addProperty("description", description);
        
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        JsonObject valueProperty = new JsonObject();
        valueProperty.addProperty("type", getJsonType(paramClass));
        properties.add("value", valueProperty);
        
        parameters.add("properties", properties);
        
        schema.add("parameters", parameters);
        
        return schema;
    }
    
    private JsonObject createEmptySchema(String name, String description) {
        JsonObject schema = new JsonObject();
        schema.addProperty("name", name);
        schema.addProperty("description", description);
        
        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");
        parameters.add("properties", new JsonObject());
        
        schema.add("parameters", parameters);
        
        return schema;
    }
    
    private String getJsonType(Class<?> clazz) {
        if (clazz == String.class) {
            return "string";
        } else if (clazz == Integer.class || clazz == Long.class || clazz == int.class || clazz == long.class) {
            return "integer";
        } else if (clazz == Double.class || clazz == Float.class || clazz == double.class || clazz == float.class) {
            return "number";
        } else if (clazz == Boolean.class || clazz == boolean.class) {
            return "boolean";
        }
        return "string";
    }
}
