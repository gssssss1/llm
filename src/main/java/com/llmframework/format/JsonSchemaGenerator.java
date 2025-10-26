package com.llmframework.format;

import com.llmframework.util.JsonUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Simple JSON Schema generator
 * In production, use a proper JSON Schema library
 */
public class JsonSchemaGenerator {
    
    public static String generate(Class<?> type) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", generateProperties(type));
        schema.put("required", getRequiredFields(type));
        
        return JsonUtils.toJson(schema);
    }
    
    private static Map<String, Object> generateProperties(Class<?> type) {
        Map<String, Object> properties = new HashMap<>();
        
        for (Field field : type.getDeclaredFields()) {
            Map<String, Object> property = new HashMap<>();
            property.put("type", mapJavaTypeToJsonType(field.getType()));
            properties.put(field.getName(), property);
        }
        
        return properties;
    }
    
    private static List<String> getRequiredFields(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
            .map(Field::getName)
            .toList();
    }
    
    private static String mapJavaTypeToJsonType(Class<?> type) {
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
