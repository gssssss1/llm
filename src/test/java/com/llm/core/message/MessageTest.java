package com.llm.core.message;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    @Test
    void testUserMessageCreation() {
        UserMessage message = UserMessage.of("Hello");
        
        assertEquals(MessageType.USER, message.getType());
        assertEquals("Hello", message.getTextContent());
        assertNotNull(message.getId());
        assertNotNull(message.getTimestamp());
    }

    @Test
    void testSystemMessageCreation() {
        SystemMessage message = SystemMessage.of("You are helpful");
        
        assertEquals(MessageType.SYSTEM, message.getType());
        assertEquals("You are helpful", message.getTextContent());
    }

    @Test
    void testAssistantMessageCreation() {
        AssistantMessage message = AssistantMessage.of("I'm here to help");
        
        assertEquals(MessageType.ASSISTANT, message.getType());
        assertEquals("I'm here to help", message.getTextContent());
    }

    @Test
    void testToolMessageCreation() {
        ToolMessage message = ToolMessage.of("call-123", "weather", "25C sunny");
        
        assertEquals(MessageType.TOOL, message.getType());
        assertEquals("weather", message.getToolName());
        assertEquals("call-123", message.getToolCallId());
        assertEquals("25C sunny", message.getTextContent());
    }

    @Test
    void testMessageBuilder() {
        UserMessage message = UserMessage.builder()
                .text("Test message")
                .metadata("key", "value")
                .build();
        
        assertEquals("Test message", message.getTextContent());
        assertEquals("value", message.getMetadata().get("key"));
    }
}
