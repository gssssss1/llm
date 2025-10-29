package com.jsonschema.llm.message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

public class MessageExample {
    
    public static void main(String[] args) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        
        System.out.println("=== Example 1: Basic Conversation ===");
        Conversation conversation = new Conversation();
        conversation.addSystem("You are a helpful assistant.");
        conversation.addUser("What's the weather in Paris?");
        conversation.addAssistant("Let me check the weather for you.");
        
        System.out.println(conversation);
        System.out.println();
        
        System.out.println("=== Example 2: OpenAI Format ===");
        JsonArray openAIMessages = conversation.toOpenAIFormat();
        System.out.println(gson.toJson(openAIMessages));
        System.out.println();
        
        System.out.println("=== Example 3: Anthropic Format ===");
        JsonArray anthropicMessages = conversation.toAnthropicFormat();
        System.out.println(gson.toJson(anthropicMessages));
        System.out.println();
        
        System.out.println("=== Example 4: Tool Calling Flow ===");
        Conversation toolConversation = new Conversation();
        toolConversation.addUser("What's the weather in London?");
        
        AssistantMessage assistantMsg = MessageBuilder.assistant();
        assistantMsg.addToolCall("call_123", "getWeather", "{\"city\":\"London\"}");
        toolConversation.addMessage(assistantMsg);
        
        toolConversation.addTool("call_123", "Weather in London: Sunny, 20°C");
        
        toolConversation.addAssistant("The weather in London is sunny with a temperature of 20°C.");
        
        System.out.println(toolConversation);
        System.out.println();
        
        System.out.println("=== Example 5: Tool Calling in OpenAI Format ===");
        System.out.println(gson.toJson(toolConversation.toOpenAIFormat()));
        System.out.println();
        
        System.out.println("=== Example 6: Building Messages with Builder ===");
        Message systemMsg = MessageBuilder.system("You are a helpful AI assistant.");
        Message userMsg = MessageBuilder.user("Hello!");
        Message assistantMsg2 = MessageBuilder.assistant("Hi! How can I help you?");
        
        Conversation builderConversation = new Conversation()
            .addMessage(systemMsg)
            .addMessage(userMsg)
            .addMessage(assistantMsg2);
        
        System.out.println(builderConversation);
        System.out.println();
        
        System.out.println("=== Example 7: Message Metadata ===");
        UserMessage userWithMeta = MessageBuilder.user("Calculate 10 + 5");
        userWithMeta.addMetadata("timestamp", System.currentTimeMillis());
        userWithMeta.addMetadata("user_id", "user_123");
        
        System.out.println("Message: " + userWithMeta);
        System.out.println("Metadata: " + userWithMeta.getMetadata());
        System.out.println();
        
        System.out.println("=== Example 8: Multiple Tool Calls ===");
        AssistantMessage multiToolMsg = MessageBuilder.assistant();
        multiToolMsg.addToolCall("call_1", "getWeather", "{\"city\":\"Paris\"}");
        multiToolMsg.addToolCall("call_2", "getWeather", "{\"city\":\"Tokyo\"}");
        multiToolMsg.addToolCall("call_3", "getTime", "{}");
        
        System.out.println("Has tool calls: " + multiToolMsg.hasToolCalls());
        System.out.println("Number of tool calls: " + multiToolMsg.getToolCalls().size());
        System.out.println(gson.toJson(multiToolMsg.toOpenAIFormat()));
    }
}
