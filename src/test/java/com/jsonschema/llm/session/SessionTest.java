package com.jsonschema.llm.session;

import com.jsonschema.llm.session.context.ContextManager;
import com.jsonschema.llm.session.context.MessageFilter;
import com.jsonschema.llm.session.event.*;
import com.jsonschema.llm.session.memory.Memory;
import com.jsonschema.llm.session.memory.ShortTermMemory;
import com.jsonschema.llm.message.Message;
import com.jsonschema.llm.message.MessageRole;
import com.jsonschema.llm.message.UserMessage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SessionTest {
    
    @Test
    public void testShortTermMemory() {
        Memory memory = new ShortTermMemory(10);
        
        UserMessage msg = new UserMessage("Test");
        memory.add(msg);
        
        assertEquals(1, memory.size());
        assertEquals(1, memory.getMessages().size());
        
        memory.add("key", "value");
        assertEquals("value", memory.get("key"));
    }
    
    @Test
    public void testShortTermMemoryLimit() {
        Memory memory = new ShortTermMemory(5);
        
        for (int i = 0; i < 10; i++) {
            memory.add(new UserMessage("Message " + i));
        }
        
        assertEquals(5, memory.size());
    }
    
    @Test
    public void testContextManager() {
        ContextManager manager = new ContextManager(1000);
        
        UserMessage msg = new UserMessage("Test message");
        manager.addMessage(msg);
        
        assertEquals(1, manager.size());
        assertFalse(manager.isEmpty());
    }
    
    @Test
    public void testContextManagerQuery() {
        ContextManager manager = new ContextManager(1000);
        
        manager.addMessage(new UserMessage("User 1"));
        manager.addMessage(new UserMessage("User 2"));
        
        List<Message> userMessages = manager.query(
            MessageFilter.byRole(MessageRole.USER)
        );
        
        assertEquals(2, userMessages.size());
    }
    
    @Test
    public void testMessageFilter() {
        UserMessage msg1 = new UserMessage("Hello");
        UserMessage msg2 = new UserMessage("World");
        
        MessageFilter filter = MessageFilter.byContent("Hello");
        
        assertTrue(filter.accept(msg1));
        assertFalse(filter.accept(msg2));
    }
    
    @Test
    public void testEvents() {
        String sessionId = "test_session";
        
        UserMessageEvent userEvent = new UserMessageEvent(
            sessionId, "Hello", "user_123"
        );
        
        assertEquals(EventType.USER_MESSAGE, userEvent.getType());
        assertEquals("Hello", userEvent.getContent());
        assertEquals("user_123", userEvent.getUserId());
        assertNotNull(userEvent.getId());
        assertNotNull(userEvent.getTimestamp());
    }
    
    @Test
    public void testToolCallEvent() {
        String sessionId = "test_session";
        
        ToolCallEvent event = new ToolCallEvent(
            sessionId,
            "getWeather",
            "call_123",
            "{\"city\":\"Tokyo\"}"
        );
        
        assertEquals(EventType.TOOL_CALL, event.getType());
        assertEquals("getWeather", event.getToolName());
        assertEquals("call_123", event.getToolCallId());
    }
    
    @Test
    public void testToolResultEvent() {
        String sessionId = "test_session";
        
        ToolResultEvent successEvent = ToolResultEvent.success(
            sessionId,
            "call_123",
            "getWeather",
            "Sunny, 20°C"
        );
        
        assertTrue(successEvent.isSuccess());
        assertEquals("Sunny, 20°C", successEvent.getResult());
        
        ToolResultEvent failureEvent = ToolResultEvent.failure(
            sessionId,
            "call_124",
            "getWeather",
            "API Error"
        );
        
        assertFalse(failureEvent.isSuccess());
        assertEquals("API Error", failureEvent.getError());
    }
    
    @Test
    public void testContextCompression() {
        ContextManager manager = new ContextManager(100);
        
        for (int i = 0; i < 20; i++) {
            manager.addMessage(new UserMessage("Message " + i + " with some content"));
        }
        
        List<Message> compressed = manager.compress();
        
        assertTrue(compressed.size() < 20);
        
        int totalTokens = compressed.stream()
            .mapToInt(msg -> (int) Math.ceil(msg.getContent().length() / 4.0))
            .sum();
        
        assertTrue(totalTokens <= 100);
    }
}
