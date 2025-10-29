package com.jsonschema.tool.interceptor;

import java.util.Arrays;

public class LoggingInterceptor implements ToolInterceptor {
    
    @Override
    public void before(ToolInvocation invocation) {
        System.out.println("[TOOL] Calling: " + invocation.getToolName());
        if (invocation.getArgs() != null && invocation.getArgs().length > 0) {
            System.out.println("[TOOL] Arguments: " + Arrays.toString(invocation.getArgs()));
        }
    }
    
    @Override
    public void after(ToolInvocation invocation, Object result) {
        System.out.println("[TOOL] Completed: " + invocation.getToolName() + 
                         " in " + invocation.getElapsedTime() + "ms");
        System.out.println("[TOOL] Result: " + result);
    }
    
    @Override
    public void onError(ToolInvocation invocation, Exception error) {
        System.err.println("[TOOL] Error in: " + invocation.getToolName());
        System.err.println("[TOOL] Error message: " + error.getMessage());
    }
}
