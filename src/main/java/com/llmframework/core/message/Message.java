package com.llmframework.core.message;

import com.llmframework.tool.ToolCall;
import com.llmframework.core.usage.TokenCounter;

import java.util.List;

/**
 * Message interface - supports multimodal content
 */
public sealed interface Message permits TextMessage, SystemMessage, AssistantMessage, ToolMessage {
    
    MessageRole role();
    String content();
    String name();
    int estimateTokens();
    
    // Convenience factory methods
    static Message system(String content) {
        return new SystemMessage(content);
    }
    
    static Message user(String content) {
        return new TextMessage(MessageRole.USER, content, null);
    }
    
    static Message assistant(String content) {
        return new AssistantMessage(content, null);
    }
    
    static Message assistant(String content, List<ToolCall> toolCalls) {
        return new AssistantMessage(content, toolCalls);
    }
    
    static Message tool(String toolCallId, String result) {
        return new ToolMessage(toolCallId, result);
    }
}

/**
 * Text message
 */
record TextMessage(
    MessageRole role,
    String content,
    String name
) implements Message {
    
    @Override
    public int estimateTokens() {
        return TokenCounter.estimate(content);
    }
}

/**
 * System message
 */
record SystemMessage(String content) implements Message {
    
    @Override
    public MessageRole role() {
        return MessageRole.SYSTEM;
    }
    
    @Override
    public String name() {
        return null;
    }
    
    @Override
    public int estimateTokens() {
        return TokenCounter.estimate(content);
    }
}

/**
 * Assistant message
 */
record AssistantMessage(
    String content,
    List<ToolCall> toolCalls
) implements Message {
    
    @Override
    public MessageRole role() {
        return MessageRole.ASSISTANT;
    }
    
    @Override
    public String name() {
        return null;
    }
    
    @Override
    public int estimateTokens() {
        return TokenCounter.estimate(content);
    }
    
    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }
}

/**
 * Tool message
 */
record ToolMessage(
    String toolCallId,
    String content
) implements Message {
    
    @Override
    public MessageRole role() {
        return MessageRole.TOOL;
    }
    
    @Override
    public String name() {
        return null;
    }
    
    @Override
    public int estimateTokens() {
        return TokenCounter.estimate(content);
    }
}
