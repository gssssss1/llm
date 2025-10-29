package com.jsonschema.llm.session;

import com.jsonschema.llm.client.OpenAIClient;
import com.jsonschema.llm.client.StreamCallback;
import com.jsonschema.llm.session.event.*;
import com.jsonschema.tool.ExampleTools;
import com.jsonschema.tool.ToolExecutor;

import java.util.concurrent.CountDownLatch;

public class SessionStreamingExample {
    
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
        
        ToolExecutor toolExecutor = new ToolExecutor();
        toolExecutor.registerTool(new ExampleTools());
        
        SessionManager sessionManager = new SessionManager();
        
        Session session = sessionManager.createSession(
            Session.builder()
                .userId("user_123")
                .client(client)
                .model("gpt-3.5-turbo")
                .toolExecutor(toolExecutor)
                .systemPrompt("You are a helpful assistant.")
        );
        
        session.addEventListener(new EventHandler() {
            @Override
            public void onEvent(Event event) {
                if (!(event instanceof AssistantMessageEvent)) {
                    System.out.println("\n[Event] " + event.getType());
                }
            }
            
            @Override
            public void onUserMessage(UserMessageEvent event) {
                System.out.println("[User] " + event.getContent());
            }
        });
        
        try {
            System.out.println("=== Session Streaming Example ===");
            System.out.println("Session ID: " + session.getSessionId());
            System.out.println();
            
            CountDownLatch latch = new CountDownLatch(1);
            
            System.out.print("[Assistant] ");
            
            session.sendMessageStream(
                "Tell me a short story about a robot learning to code.",
                new StreamCallback() {
                    @Override
                    public void onStart() {
                        // Streaming started
                    }
                    
                    @Override
                    public void onChunk(String content) {
                        System.out.print(content);
                        System.out.flush();
                    }
                    
                    @Override
                    public void onComplete(String fullContent) {
                        System.out.println();
                        System.out.println();
                        System.out.println("[Streaming completed]");
                        System.out.println("Content length: " + fullContent.length());
                        latch.countDown();
                    }
                    
                    @Override
                    public void onError(Exception error) {
                        System.err.println("Error: " + error.getMessage());
                        latch.countDown();
                    }
                }
            );
            
            latch.await();
            
            System.out.println();
            System.out.println("=== Session Statistics ===");
            System.out.println("Total Events: " + session.getEvents().size());
            System.out.println("Messages in Conversation: " + session.getConversation().size());
            System.out.println("Context Messages: " + session.getContextManager().size());
            
        } finally {
            client.close();
        }
    }
    
    private static void demonstrateUsage() {
        System.out.println("\n=== Session Streaming Usage ===");
        System.out.println("Session session = Session.builder()");
        System.out.println("    .client(client)");
        System.out.println("    .model(\"gpt-3.5-turbo\")");
        System.out.println("    .build();");
        System.out.println();
        System.out.println("session.sendMessageStream(\"Tell me a story\", new StreamCallback() {");
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
        System.out.println("        System.out.println(\"\\nDone!\");");
        System.out.println("    }");
        System.out.println("    ");
        System.out.println("    @Override");
        System.out.println("    public void onError(Exception error) {");
        System.out.println("        error.printStackTrace();");
        System.out.println("    }");
        System.out.println("});");
    }
}
