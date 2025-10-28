package com.jsonschema.tool;

import com.google.gson.JsonObject;
import com.jsonschema.tool.interceptor.ToolInterceptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ToolDefinition {
    private final String name;
    private final String description;
    private final Method method;
    private final Object instance;
    private final Class<?> parameterClass;
    private final JsonObject schema;
    private final List<ToolInterceptor> interceptors;
    
    public ToolDefinition(String name, String description, Method method, Object instance, 
                         Class<?> parameterClass, JsonObject schema) {
        this.name = name;
        this.description = description;
        this.method = method;
        this.instance = instance;
        this.parameterClass = parameterClass;
        this.schema = schema;
        this.interceptors = new ArrayList<>();
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
    
    public List<ToolInterceptor> getInterceptors() {
        return interceptors;
    }
    
    public void addInterceptor(ToolInterceptor interceptor) {
        interceptors.add(interceptor);
    }
    
    @Override
    public String toString() {
        return "ToolDefinition{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", parameterClass=" + (parameterClass != null ? parameterClass.getSimpleName() : "void") +
                ", interceptors=" + interceptors.size() +
                '}';
    }
}
