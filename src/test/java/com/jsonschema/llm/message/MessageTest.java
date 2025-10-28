package com.jsonschema.llm.message;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {
    
    @Test
    public void testSystemMessage() {
        SystemMessage message = new SystemMessage("You are helpful");
        assertEquals(MessageRole.SYSTEM, message.getRole());
        assertEquals("You are helpful", message.getContent());
        
        JsonObject openAI = message.toOpenAIFormat();
        assertEquals("system", openAI.get("role").getAsString());
        assertEquals("You are helpful", openAI.get("content").getAsString());
    }
    
    @Test
    public void testUserMessage() {
        UserMessage message = UserMessage.of("Hello");
        assertEquals(MessageRole.USER, message.getRole());
        assertEquals("Hello", message.getContent());
    }
    
    @Test
    public void testAssistantMessage() {
        AssistantMessage message = AssistantMessage.of("Hi there");
        assertEquals(MessageRole.ASSISTANT, message.getRole());
        assertEquals("Hi there", message.getContent());
        assertFalse(message.hasToolCalls());
    }
    
    @Test
    public void testAssistantMessageWithToolCalls() {
        AssistantMessage message = new AssistantMessage();
        message.addToolCall("call_1", "getWeather", "{\"city\":\"Paris\"}");
        
        assertTrue(message.hasToolCalls());
        assertEquals(1, message.getToolCalls().size());
        
        ToolCall toolCall = message.getToolCalls().get(0);
        assertEquals("call_1", toolCall.getId());
        assertEquals("getWeather", toolCall.getName());
    }
    
    @Test
    public void testToolMessage() {
        ToolMessage message = ToolMessage.of("call_123", "Weather result");
        assertEquals(MessageRole.TOOL, message.getRole());
        assertEquals("call_123", message.getToolCallId());
        assertEquals("Weather result", message.getContent());
    }
    
    @Test
    public void testConversation() {
        Conversation conversation = new Conversation();
        conversation.addUser("Hello");
        conversation.addAssistant("Hi");
        
        assertEquals(2, conversation.size());
        assertFalse(conversation.isEmpty());
        
        Message lastMessage = conversation.getLastMessage();
        assertEquals(MessageRole.ASSISTANT, lastMessage.getRole());
    }
    
    @Test
    public void testConversationToOpenAI() {
        Conversation conversation = new Conversation();
        conversation.addSystem("System prompt");
        conversation.addUser("User message");
        
        JsonArray array = conversation.toOpenAIFormat();
        assertEquals(2, array.size());
        
        JsonObject first = array.get(0).getAsJsonObject();
        assertEquals("system", first.get("role").getAsString());
    }
    
    @Test
    public void testMessageBuilder() {
        Message system = MessageBuilder.system("System");
        Message user = MessageBuilder.user("User");
        Message assistant = MessageBuilder.assistant("Assistant");
        Message tool = MessageBuilder.tool("call_1", "Result");
        
        assertEquals(MessageRole.SYSTEM, system.getRole());
        assertEquals(MessageRole.USER, user.getRole());
        assertEquals(MessageRole.ASSISTANT, assistant.getRole());
        assertEquals(MessageRole.TOOL, tool.getRole());
    }
    
    @Test
    public void testMessageMetadata() {
        UserMessage message = MessageBuilder.user("Test");
        message.addMetadata("key1", "value1");
        message.addMetadata("key2", 123);
        
        assertEquals("value1", message.getMetadata("key1"));
        assertEquals(123, message.getMetadata("key2"));
    }
    
    @Test
    public void testToolCallFormat() {
        ToolCall toolCall = new ToolCall("call_abc", "testTool", "{\"arg\":\"value\"}");
        
        JsonObject openAI = toolCall.toOpenAIFormat();
        assertEquals("call_abc", openAI.get("id").getAsString());
        assertEquals("function", openAI.get("type").getAsString());
        assertTrue(openAI.has("function"));
        
        JsonObject function = openAI.getAsJsonObject("function");
        assertEquals("testTool", function.get("name").getAsString());
    }
}
