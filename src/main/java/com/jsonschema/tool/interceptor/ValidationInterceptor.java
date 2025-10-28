package com.jsonschema.tool.interceptor;

public class ValidationInterceptor implements ToolInterceptor {
    
    @Override
    public void before(ToolInvocation invocation) throws Exception {
        if (invocation.getArgs() != null) {
            for (Object arg : invocation.getArgs()) {
                if (arg == null) {
                    throw new IllegalArgumentException("Tool argument cannot be null");
                }
            }
        }
    }
}
