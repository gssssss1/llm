package com.jsonschema.tool;

import com.jsonschema.annotations.Tool;
import com.jsonschema.tool.interceptor.LoggingInterceptor;
import com.jsonschema.tool.interceptor.TimingInterceptor;
import com.jsonschema.tool.interceptor.ValidationInterceptor;

import java.util.HashMap;
import java.util.Map;

public class InterceptorExample {
    
    public static void main(String[] args) throws Exception {
        ToolExecutor executor = new ToolExecutor();
        
        executor.addGlobalInterceptor(new TimingInterceptor());
        
        InterceptorTools tools = new InterceptorTools();
        executor.registerTool(tools);
        
        System.out.println("=== Example 1: Tool with Logging Interceptor ===");
        Map<String, Object> params = new HashMap<>();
        params.put("message", "Hello World");
        executor.executeTool("logMessage", params);
        System.out.println();
        
        System.out.println("=== Example 2: Tool with Validation ===");
        Map<String, Object> params2 = new HashMap<>();
        params2.put("value", 42);
        executor.executeTool("validateNumber", params2);
        System.out.println();
        
        System.out.println("=== Example 3: Tool with Multiple Interceptors ===");
        Map<String, Object> params3 = new HashMap<>();
        params3.put("a", 10);
        params3.put("b", 5);
        executor.executeTool("complexOperation", params3);
        System.out.println();
        
        System.out.println("=== Example 4: Tool without Interceptors ===");
        executor.executeTool("simpleOperation");
    }
    
    static class InterceptorTools {
        
        @Tool(description = "Log a message", interceptors = {LoggingInterceptor.class})
        public String logMessage(MessageRequest request) {
            return "Logged: " + request.getMessage();
        }
        
        @Tool(description = "Validate a number", interceptors = {ValidationInterceptor.class, LoggingInterceptor.class})
        public String validateNumber(NumberRequest request) {
            return "Number " + request.getValue() + " is valid";
        }
        
        @Tool(description = "Complex operation", interceptors = {LoggingInterceptor.class, TimingInterceptor.class})
        public int complexOperation(OperationRequest request) throws InterruptedException {
            Thread.sleep(100);
            return request.getA() + request.getB();
        }
        
        @Tool(description = "Simple operation without interceptors")
        public String simpleOperation() {
            return "Simple result";
        }
    }
    
    static class MessageRequest {
        private String message;
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
    
    static class NumberRequest {
        private int value;
        
        public int getValue() {
            return value;
        }
        
        public void setValue(int value) {
            this.value = value;
        }
    }
    
    static class OperationRequest {
        private int a;
        private int b;
        
        public int getA() {
            return a;
        }
        
        public void setA(int a) {
            this.a = a;
        }
        
        public int getB() {
            return b;
        }
        
        public void setB(int b) {
            this.b = b;
        }
    }
}
