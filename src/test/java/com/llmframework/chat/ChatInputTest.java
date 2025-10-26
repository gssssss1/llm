package com.llmframework.chat;

import com.llmframework.core.message.Message;
import com.llmframework.core.model.ModelOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatInputTest {
    
    @Test
    void testSimpleCreation() {
        ChatInput input = ChatInput.of("Hello");
        
        assertNotNull(input);
        assertNotNull(input.requestId());
        assertEquals(1, input.messages().size());
        assertEquals("Hello", input.messages().get(0).content());
    }
    
    @Test
    void testBuilder() {
        ChatInput input = ChatInput.builder()
            .system("You are helpful")
            .user("What is AI?")
            .options(opts -> opts
                .temperature(0.8)
                .maxTokens(100))
            .build();
        
        assertEquals(2, input.messages().size());
        assertEquals(0.8, input.options().temperature());
        assertEquals(100, input.options().maxTokens());
    }
    
    @Test
    void testTokenEstimation() {
        ChatInput input = ChatInput.builder()
            .message(Message.user("Hello world"))
            .build();
        
        assertTrue(input.estimateTokens().promptTokens() > 0);
    }
    
    @Test
    void testImmutability() {
        ChatInput input = ChatInput.of("Test");
        
        // Messages list should be immutable
        assertThrows(UnsupportedOperationException.class, () -> 
            input.messages().add(Message.user("Another"))
        );
    }
}
