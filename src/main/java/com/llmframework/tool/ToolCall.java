package com.llmframework.tool;

import com.llmframework.util.JsonUtils;

import java.util.List;
import java.util.Map;

/**
 * Represents a tool/function call
 */
public record ToolCall(
    String id,
    String name,
    Map<String, Object> arguments
) {
    
    public <T> T getArgument(String name, Class<T> type) {
        Object value = arguments.get(name);
        if (value == null) return null;
        return JsonUtils.convert(value, type);
    }
    
    public String getString(String name) {
        return getArgument(name, String.class);
    }
    
    public Integer getInt(String name) {
        return getArgument(name, Integer.class);
    }
    
    public Double getDouble(String name) {
        return getArgument(name, Double.class);
    }
    
    public Boolean getBoolean(String name) {
        return getArgument(name, Boolean.class);
    }
    
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String name, Class<T> elementType) {
        Object value = arguments.get(name);
        if (value instanceof List<?> list) {
            return list.stream()
                .map(item -> JsonUtils.convert(item, elementType))
                .toList();
        }
        return List.of();
    }
}
