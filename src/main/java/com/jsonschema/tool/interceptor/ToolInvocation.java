package com.jsonschema.tool.interceptor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ToolInvocation {
    private final String toolName;
    private final Method method;
    private final Object instance;
    private final Object[] args;
    private final Map<String, Object> context;
    private final long startTime;
    
    public ToolInvocation(String toolName, Method method, Object instance, Object[] args) {
        this.toolName = toolName;
        this.method = method;
        this.instance = instance;
        this.args = args;
        this.context = new HashMap<>();
        this.startTime = System.currentTimeMillis();
    }
    
    public String getToolName() {
        return toolName;
    }
    
    public Method getMethod() {
        return method;
    }
    
    public Object getInstance() {
        return instance;
    }
    
    public Object[] getArgs() {
        return args;
    }
    
    public Map<String, Object> getContext() {
        return context;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }
    
    public void setAttribute(String key, Object value) {
        context.put(key, value);
    }
    
    public Object getAttribute(String key) {
        return context.get(key);
    }
}
