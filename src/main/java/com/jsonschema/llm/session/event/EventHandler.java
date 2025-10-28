package com.jsonschema.llm.session.event;

public interface EventHandler {
    
    void onEvent(Event event);
    
    default void onUserMessage(UserMessageEvent event) {
    }
    
    default void onAssistantMessage(AssistantMessageEvent event) {
    }
    
    default void onToolCall(ToolCallEvent event) {
    }
    
    default void onToolResult(ToolResultEvent event) {
    }
    
    default void onThinking(ThinkingEvent event) {
    }
}
