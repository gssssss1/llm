package com.jsonschema.tool.interceptor;

import java.lang.reflect.Method;

public interface ToolInterceptor {
    
    default void before(ToolInvocation invocation) throws Exception {
    }
    
    default void after(ToolInvocation invocation, Object result) throws Exception {
    }
    
    default void onError(ToolInvocation invocation, Exception error) throws Exception {
    }
}
