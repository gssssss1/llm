package com.jsonschema.llm.client;

import com.jsonschema.llm.client.model.ChatRequest;
import com.jsonschema.llm.client.model.ChatRequestBuilder;
import com.jsonschema.llm.message.MessageBuilder;

import java.util.concurrent.CountDownLatch;

public class StreamingExample {
    
    public static void main(String[] args) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("Please set OPENAI_API_KEY environment variable");
            demonstrateUsage();
            return;
        }
        
        try {
            runRealExample(apiKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void runRealExample(String apiKey) throws Exception {
        OpenAIClient client = OpenAIClient.builder()
                .apiKey(apiKey)
                .build();
        
        try {
            System.out.println("=== Example 1: Basic Streaming ===");
            basicStreaming(client);
            System.out.println();
            
            System.out.println("=== Example 2: Streaming with Callback ===");
            streamingWithCallback(client);
            System.out.println();
            
        } finally {
            client.close();
        }
    }
    
    private static void basicStreaming(OpenAIClient client) throws Exception {
        ChatRequest request = ChatRequestBuilder.create("gpt-3.5-turbo")
                .message(MessageBuilder.system("You are a helpful assistant."))
                .message(MessageBuilder.user("Tell me a short joke about programming."))
                .temperature(0.7)
                .maxTokens(100)
                .build();
        
        CountDownLatch latch = new CountDownLatch(1);
        
        client.chatStream(request, new StreamCallback() {
            @Override
            public void onStart() {
                System.out.println("[Streaming started]");
                System.out.print("Assistant: ");
            }
            
            @Override
            public void onChunk(String content) {
                System.out.print(content);
                System.out.flush();
            }
            
            @Override
            public void onComplete(String fullContent) {
                System.out.println();
                System.out.println("[Streaming completed]");
                System.out.println("Full content length: " + fullContent.length());
                latch.countDown();
            }
            
            @Override
            public void onError(Exception error) {
                System.err.println("Error: " + error.getMessage());
                latch.countDown();
            }
        });
        
        latch.await();
    }
    
    private static void streamingWithCallback(OpenAIClient client) throws Exception {
        ChatRequest request = ChatRequestBuilder.create("gpt-3.5-turbo")
                .message(MessageBuilder.user("Explain in 3 sentences what is AI."))
                .temperature(0.5)
                .maxTokens(150)
                .build();
        
        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder fullResponse = new StringBuilder();
        
        client.chatStream(request, new StreamCallback() {
            private int chunkCount = 0;
            
            @Override
            public void onStart() {
                System.out.println("[Stream started]");
            }
            
            @Override
            public void onChunk(String content) {
                chunkCount++;
                fullResponse.append(content);
                System.out.print(content);
                System.out.flush();
            }
            
            @Override
            public void onComplete(String fullContent) {
                System.out.println();
                System.out.println("[Stream completed]");
                System.out.println("Total chunks received: " + chunkCount);
                System.out.println("Total characters: " + fullContent.length());
                latch.countDown();
            }
            
            @Override
            public void onError(Exception error) {
                System.err.println("Streaming error: " + error.getMessage());
                error.printStackTrace();
                latch.countDown();
            }
        });
        
        latch.await();
    }
    
    private static void demonstrateUsage() {
        System.out.println("\n=== Streaming Usage Example ===");
        System.out.println("OpenAIClient client = OpenAIClient.builder()");
        System.out.println("    .apiKey(\"your-api-key\")");
        System.out.println("    .build();");
        System.out.println();
        System.out.println("ChatRequest request = ChatRequestBuilder.create(\"gpt-3.5-turbo\")");
        System.out.println("    .message(MessageBuilder.user(\"Tell me a story\"))");
        System.out.println("    .build();");
        System.out.println();
        System.out.println("client.chatStream(request, new StreamCallback() {");
        System.out.println("    @Override");
        System.out.println("    public void onStart() {");
        System.out.println("        System.out.println(\"Streaming started\");");
        System.out.println("    }");
        System.out.println("    ");
        System.out.println("    @Override");
        System.out.println("    public void onChunk(String content) {");
        System.out.println("        System.out.print(content);");
        System.out.println("    }");
        System.out.println("    ");
        System.out.println("    @Override");
        System.out.println("    public void onComplete(String fullContent) {");
        System.out.println("        System.out.println(\"\\nCompleted!\");");
        System.out.println("    }");
        System.out.println("    ");
        System.out.println("    @Override");
        System.out.println("    public void onError(Exception error) {");
        System.out.println("        error.printStackTrace();");
        System.out.println("    }");
        System.out.println("});");
    }
}
