package com.jsonschema.llm.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jsonschema.llm.client.model.ChatRequest;
import com.jsonschema.llm.client.model.ChatRequestBuilder;
import com.jsonschema.llm.client.model.ChatResponse;
import com.jsonschema.llm.message.*;
import com.jsonschema.tool.ExampleTools;
import com.jsonschema.tool.ToolExecutor;

import java.io.IOException;

public class RealLLMClientExample {
    
    public static void main(String[] args) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("Please set OPENAI_API_KEY environment variable");
            System.out.println("\nThis example demonstrates how to use the LLM client:");
            demonstrateUsage();
            return;
        }
        
        try {
            runRealExample(apiKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void runRealExample(String apiKey) throws IOException {
        OpenAIClient client = OpenAIClient.builder()
                .apiKey(apiKey)
                .connectTimeout(30)
                .readTimeout(60)
                .build();
        
        try {
            System.out.println("=== Example 1: Simple Chat ===");
            simpleChat(client);
            System.out.println();
            
            System.out.println("=== Example 2: Chat with Tools ===");
            chatWithTools(client);
            System.out.println();
            
        } finally {
            client.close();
        }
    }
    
    private static void simpleChat(OpenAIClient client) throws IOException {
        ChatRequest request = ChatRequestBuilder.create("gpt-3.5-turbo")
                .message(MessageBuilder.system("You are a helpful assistant."))
                .message(MessageBuilder.user("Tell me a short joke about programming."))
                .temperature(0.7)
                .maxTokens(100)
                .build();
        
        ChatResponse response = client.chat(request);
        
        System.out.println("Response ID: " + response.getId());
        System.out.println("Model: " + response.getModel());
        System.out.println("Assistant: " + response.getAssistantMessage().getContent());
        System.out.println("Usage: " + response.getUsage());
    }
    
    private static void chatWithTools(OpenAIClient client) throws IOException {
        ToolExecutor toolExecutor = new ToolExecutor();
        toolExecutor.registerTool(new ExampleTools());
        
        Conversation conversation = new Conversation();
        conversation.addUser("What's the weather in Tokyo?");
        
        ChatRequest request = ChatRequestBuilder.create("gpt-3.5-turbo")
                .conversation(conversation)
                .tools(toolExecutor)
                .temperature(0.7)
                .maxTokens(500)
                .build();
        
        ChatResponse response = client.chat(request);
        
        AssistantMessage assistantMsg = response.getAssistantMessage();
        System.out.println("Finish Reason: " + response.getFirstChoice().getFinishReason());
        
        if (assistantMsg.hasToolCalls()) {
            System.out.println("Tool calls requested:");
            for (ToolCall toolCall : assistantMsg.getToolCalls()) {
                System.out.println("  - " + toolCall.getName() + ": " + toolCall.getArguments());
                
                try {
                    Object result = toolExecutor.executeTool(
                        toolCall.getName(),
                        toolCall.getArguments()
                    );
                    System.out.println("  Result: " + result);
                    
                    conversation.addMessage(assistantMsg);
                    conversation.addTool(toolCall.getId(), result.toString());
                    
                } catch (Exception e) {
                    System.err.println("  Error executing tool: " + e.getMessage());
                }
            }
            
            ChatRequest followUpRequest = ChatRequestBuilder.create("gpt-3.5-turbo")
                    .conversation(conversation)
                    .tools(toolExecutor)
                    .build();
            
            ChatResponse followUpResponse = client.chat(followUpRequest);
            System.out.println("\nFinal Response: " + followUpResponse.getAssistantMessage().getContent());
        } else {
            System.out.println("Response: " + assistantMsg.getContent());
        }
        
        System.out.println("Usage: " + response.getUsage());
    }
    
    private static void demonstrateUsage() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        
        System.out.println("\n=== OpenAI Client Usage ===");
        System.out.println("OpenAIClient client = OpenAIClient.builder()");
        System.out.println("    .apiKey(\"your-api-key\")");
        System.out.println("    .baseUrl(\"https://api.openai.com/v1\") // optional");
        System.out.println("    .connectTimeout(30) // optional");
        System.out.println("    .readTimeout(60)    // optional");
        System.out.println("    .build();");
        System.out.println();
        System.out.println("ChatRequest request = ChatRequestBuilder.create(\"gpt-3.5-turbo\")");
        System.out.println("    .message(MessageBuilder.user(\"Hello!\"))");
        System.out.println("    .temperature(0.7)");
        System.out.println("    .maxTokens(100)");
        System.out.println("    .build();");
        System.out.println();
        System.out.println("ChatResponse response = client.chat(request);");
        System.out.println("System.out.println(response.getAssistantMessage().getContent());");
        
        System.out.println("\n=== Anthropic Client Usage ===");
        System.out.println("AnthropicClient client = AnthropicClient.builder()");
        System.out.println("    .apiKey(\"your-api-key\")");
        System.out.println("    .build();");
        System.out.println();
        System.out.println("ChatRequest request = ChatRequestBuilder.create(\"claude-3-opus-20240229\")");
        System.out.println("    .message(MessageBuilder.user(\"Hello!\"))");
        System.out.println("    .maxTokens(1024)");
        System.out.println("    .build();");
        System.out.println();
        System.out.println("ChatResponse response = client.chat(request);");
        System.out.println("System.out.println(response.getAssistantMessage().getContent());");
        
        System.out.println("\n=== With Tools ===");
        System.out.println("ToolExecutor toolExecutor = new ToolExecutor();");
        System.out.println("toolExecutor.registerTool(new MyTools());");
        System.out.println();
        System.out.println("ChatRequest request = ChatRequestBuilder.create(\"gpt-4\")");
        System.out.println("    .message(MessageBuilder.user(\"What's the weather?\"))");
        System.out.println("    .tools(toolExecutor)");
        System.out.println("    .build();");
        System.out.println();
        System.out.println("ChatResponse response = client.chat(request);");
        System.out.println();
        System.out.println("if (response.getAssistantMessage().hasToolCalls()) {");
        System.out.println("    for (ToolCall toolCall : response.getAssistantMessage().getToolCalls()) {");
        System.out.println("        Object result = toolExecutor.executeTool(");
        System.out.println("            toolCall.getName(),");
        System.out.println("            toolCall.getArguments()");
        System.out.println("        );");
        System.out.println("        // Add tool result back to conversation");
        System.out.println("    }");
        System.out.println("}");
    }
}
