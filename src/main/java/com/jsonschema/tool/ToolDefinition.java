package com.jsonschema.tool;

import com.google.gson.JsonObject;

import java.lang.reflect.Method;

public class ToolDefinition {
    private final String name;
    private final String description;
    private final Method method;
    private final Object instance;
    private final Class<?> parameterClass;
    private final JsonObject schema;
    
    public ToolDefinition(String name, String description, Method method, Object instance, 
                         Class<?> parameterClass, JsonObject schema) {
        this.name = name;
        this.description = description;
        this.method = method;
        this.instance = instance;
        this.parameterClass = parameterClass;
        this.schema = schema;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Method getMethod() {
        return method;
    }
    
    public Object getInstance() {
        return instance;
    }
    
    public Class<?> getParameterClass() {
        return parameterClass;
    }
    
    public JsonObject getSchema() {
        return schema;
    }
    
    @Override
    public String toString() {
        return "ToolDefinition{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", parameterClass=" + (parameterClass != null ? parameterClass.getSimpleName() : "void") +
                '}';
    }
}
