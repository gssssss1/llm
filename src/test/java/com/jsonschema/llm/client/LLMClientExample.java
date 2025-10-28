package com.jsonschema.llm.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jsonschema.llm.client.model.ChatRequest;
import com.jsonschema.llm.client.model.ChatRequestBuilder;
import com.jsonschema.llm.client.model.ChatResponse;
import com.jsonschema.llm.message.Conversation;
import com.jsonschema.llm.message.MessageBuilder;
import com.jsonschema.tool.ToolExecutor;
import com.jsonschema.tool.WeatherRequest;

public class LLMClientExample {
    
    public static void main(String[] args) {
        demonstrateBasicUsage();
        demonstrateWithTools();
        demonstrateConversation();
    }
    
    private static void demonstrateBasicUsage() {
        System.out.println("=== Example 1: Basic Chat Request ===");
        
        ChatRequest request = ChatRequestBuilder.create("gpt-4")
                .message(MessageBuilder.system("You are a helpful assistant."))
                .message(MessageBuilder.user("What is 2+2?"))
                .temperature(0.7)
                .maxTokens(100)
                .build();
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println("Request (OpenAI format):");
        System.out.println(gson.toJson(request.toOpenAIJson()));
        System.out.println();
        
        System.out.println("Request (Anthropic format):");
        System.out.println(gson.toJson(request.toAnthropicJson()));
        System.out.println();
    }
    
    private static void demonstrateWithTools() {
        System.out.println("=== Example 2: Chat with Tools ===");
        
        ToolExecutor toolExecutor = new ToolExecutor();
        toolExecutor.registerTool(new WeatherTools());
        
        ChatRequest request = ChatRequestBuilder.create("gpt-4")
                .message(MessageBuilder.system("You are a weather assistant."))
                .message(MessageBuilder.user("What's the weather in Paris?"))
                .tools(toolExecutor)
                .temperature(0.7)
                .maxTokens(500)
                .build();
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println("Request with tools:");
        System.out.println(gson.toJson(request.toOpenAIJson()));
        System.out.println();
    }
    
    private static void demonstrateConversation() {
        System.out.println("=== Example 3: Multi-turn Conversation ===");
        
        Conversation conversation = new Conversation();
        conversation.addSystem("You are a helpful math tutor.");
        conversation.addUser("What is 10 + 15?");
        conversation.addAssistant("10 + 15 equals 25.");
        conversation.addUser("And what is 25 * 2?");
        
        ChatRequest request = ChatRequestBuilder.create("gpt-4")
                .conversation(conversation)
                .temperature(0.5)
                .build();
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println("Conversation request:");
        System.out.println(gson.toJson(request.toOpenAIJson()));
        System.out.println();
    }
    
    static class WeatherTools {
        @com.jsonschema.annotations.Tool(description = "Get weather for a city")
        public String getWeather(WeatherRequest request) {
            return "Weather in " + request.getCity() + ": Sunny, 20°C";
        }
    }
}
