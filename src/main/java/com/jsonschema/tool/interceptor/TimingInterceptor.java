package com.jsonschema.tool.interceptor;

public class TimingInterceptor implements ToolInterceptor {
    
    @Override
    public void before(ToolInvocation invocation) {
        invocation.setAttribute("startTime", System.currentTimeMillis());
    }
    
    @Override
    public void after(ToolInvocation invocation, Object result) {
        long startTime = (long) invocation.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("[TIMING] Tool '" + invocation.getToolName() + 
                         "' executed in " + duration + "ms");
    }
}
