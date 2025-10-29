package com.jsonschema.llm.session;

import com.jsonschema.llm.client.OpenAIClient;
import com.jsonschema.llm.session.event.*;
import com.jsonschema.tool.ExampleTools;
import com.jsonschema.tool.ToolExecutor;

public class SessionExample {
    
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
                .systemPrompt("You are a helpful assistant with access to tools.")
        );
        
        session.addEventListener(new EventHandler() {
            @Override
            public void onEvent(Event event) {
                System.out.println("\n[Event] " + event.getType() + " at " + event.getTimestamp());
            }
            
            @Override
            public void onUserMessage(UserMessageEvent event) {
                System.out.println("[User] " + event.getContent());
            }
            
            @Override
            public void onAssistantMessage(AssistantMessageEvent event) {
                System.out.println("[Assistant] " + event.getContent());
                System.out.println("[Tokens] " + event.getTokensUsed());
            }
            
            @Override
            public void onToolCall(ToolCallEvent event) {
                System.out.println("[Tool Call] " + event.getToolName());
                System.out.println("[Arguments] " + event.getArguments());
            }
            
            @Override
            public void onToolResult(ToolResultEvent event) {
                if (event.isSuccess()) {
                    System.out.println("[Tool Result] " + event.getResult());
                } else {
                    System.out.println("[Tool Error] " + event.getError());
                }
            }
        });
        
        try {
            System.out.println("=== Session Started ===");
            System.out.println("Session ID: " + session.getSessionId());
            System.out.println();
            
            String response1 = session.sendMessage("What's the weather in Tokyo?");
            System.out.println("\nFinal Response: " + response1);
            System.out.println();
            
            String response2 = session.sendMessage("And what time is it now?");
            System.out.println("\nFinal Response: " + response2);
            System.out.println();
            
            System.out.println("=== Session Statistics ===");
            System.out.println("Total Events: " + session.getEvents().size());
            System.out.println("Messages in Conversation: " + session.getConversation().size());
            System.out.println("Context Tokens: " + session.getContextManager().getTotalTokens());
            System.out.println("Active Sessions: " + sessionManager.getActiveSessionCount());
            
        } finally {
            client.close();
        }
    }
    
    private static void demonstrateUsage() {
        System.out.println("\n=== Session Usage Example ===");
        System.out.println("// Create a session manager");
        System.out.println("SessionManager sessionManager = new SessionManager();");
        System.out.println();
        System.out.println("// Create a session");
        System.out.println("Session session = sessionManager.createSession(");
        System.out.println("    Session.builder()");
        System.out.println("        .userId(\"user_123\")");
        System.out.println("        .client(client)");
        System.out.println("        .model(\"gpt-3.5-turbo\")");
        System.out.println("        .toolExecutor(toolExecutor)");
        System.out.println("        .systemPrompt(\"You are helpful\")");
        System.out.println(");");
        System.out.println();
        System.out.println("// Add event listener");
        System.out.println("session.addEventListener(new EventHandler() {");
        System.out.println("    @Override");
        System.out.println("    public void onEvent(Event event) {");
        System.out.println("        System.out.println(\"Event: \" + event.getType());");
        System.out.println("    }");
        System.out.println("});");
        System.out.println();
        System.out.println("// Send messages");
        System.out.println("String response = session.sendMessage(\"Hello!\");");
        System.out.println();
        System.out.println("// Access session components");
        System.out.println("Conversation conversation = session.getConversation();");
        System.out.println("Memory memory = session.getMemory();");
        System.out.println("ContextManager contextManager = session.getContextManager();");
        System.out.println();
        System.out.println("// Manage context");
        System.out.println("contextManager.compress(); // Compress to fit token limit");
        System.out.println("contextManager.deleteMessage(0); // Delete specific message");
        System.out.println("contextManager.query(MessageFilter.byRole(MessageRole.USER));");
    }
}
