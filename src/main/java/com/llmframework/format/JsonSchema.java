package com.llmframework.format;

import com.llmframework.util.JsonUtils;

import java.util.*;

/**
 * JSON Schema builder
 */
public class JsonSchema {
    
    private final Map<String, Object> schema = new HashMap<>();
    private final Map<String, Object> properties = new HashMap<>();
    private final List<String> required = new ArrayList<>();
    
    private JsonSchema() {
        schema.put("type", "object");
    }
    
    public static JsonSchema object() {
        return new JsonSchema();
    }
    
    public JsonSchema addProperty(String name, Class<?> type, String description) {
        return addProperty(name, type, description, true);
    }
    
    public JsonSchema addProperty(String name, Class<?> type, String description, boolean required) {
        Map<String, Object> property = new HashMap<>();
        property.put("type", mapType(type));
        property.put("description", description);
        
        properties.put(name, property);
        
        if (required) {
            this.required.add(name);
        }
        
        return this;
    }
    
    public String toJson() {
        schema.put("properties", properties);
        schema.put("required", required);
        return JsonUtils.toJson(schema);
    }
    
    private String mapType(Class<?> type) {
        if (type == String.class) return "string";
        if (type == Integer.class || type == int.class) return "integer";
        if (type == Long.class || type == long.class) return "integer";
        if (type == Double.class || type == double.class) return "number";
        if (type == Float.class || type == float.class) return "number";
        if (type == Boolean.class || type == boolean.class) return "boolean";
        if (type.isArray() || Collection.class.isAssignableFrom(type)) return "array";
        return "object";
    }
}
