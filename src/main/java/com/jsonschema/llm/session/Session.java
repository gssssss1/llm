package com.jsonschema.llm.session;

import com.jsonschema.llm.client.LLMClient;
import com.jsonschema.llm.client.model.ChatRequest;
import com.jsonschema.llm.client.model.ChatRequestBuilder;
import com.jsonschema.llm.client.model.ChatResponse;
import com.jsonschema.llm.message.*;
import com.jsonschema.llm.session.context.ContextManager;
import com.jsonschema.llm.session.event.*;
import com.jsonschema.llm.session.memory.Memory;
import com.jsonschema.llm.session.memory.ShortTermMemory;
import com.jsonschema.tool.ToolCall;
import com.jsonschema.tool.ToolExecutor;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Session {
    private final String sessionId;
    private final String userId;
    private final LLMClient client;
    private final String model;
    private final ToolExecutor toolExecutor;
    
    private final List<Event> events;
    private final List<EventHandler> eventHandlers;
    private final Conversation conversation;
    private final Memory memory;
    private final ContextManager contextManager;
    
    private Instant createdAt;
    private Instant lastActivityAt;
    private boolean active;
    
    private Session(Builder builder) {
        this.sessionId = builder.sessionId != null ? builder.sessionId : UUID.randomUUID().toString();
        this.userId = builder.userId;
        this.client = builder.client;
        this.model = builder.model;
        this.toolExecutor = builder.toolExecutor;
        
        this.events = new ArrayList<>();
        this.eventHandlers = new ArrayList<>();
        this.conversation = new Conversation();
        this.memory = builder.memory != null ? builder.memory : new ShortTermMemory();
        this.contextManager = builder.contextManager != null ? builder.contextManager : new ContextManager(4096);
        
        this.createdAt = Instant.now();
        this.lastActivityAt = createdAt;
        this.active = true;
        
        if (builder.systemPrompt != null) {
            conversation.addSystem(builder.systemPrompt);
            contextManager.addMessage(new SystemMessage(builder.systemPrompt));
        }
    }
    
    public String sendMessage(String userMessage) throws IOException {
        updateActivity();
        
        UserMessageEvent userEvent = new UserMessageEvent(sessionId, userMessage, userId);
        emitEvent(userEvent);
        
        UserMessage userMsg = new UserMessage(userMessage);
        conversation.addMessage(userMsg);
        contextManager.addMessage(userMsg);
        memory.add(userMsg);
        
        ChatRequest request = ChatRequestBuilder.create(model)
                .messages(contextManager.compress())
                .tools(toolExecutor)
                .build();
        
        ChatResponse response = client.chat(request);
        AssistantMessage assistantMsg = response.getAssistantMessage();
        
        if (assistantMsg.hasToolCalls()) {
            return handleToolCalls(assistantMsg);
        }
        
        String content = assistantMsg.getContent();
        int tokensUsed = response.getUsage().getTotalTokens();
        
        AssistantMessageEvent assistantEvent = new AssistantMessageEvent(
            sessionId, content, model, tokensUsed
        );
        emitEvent(assistantEvent);
        
        conversation.addMessage(assistantMsg);
        contextManager.addMessage(assistantMsg);
        memory.add(assistantMsg);
        
        return content;
    }
    
    private String handleToolCalls(AssistantMessage assistantMsg) throws IOException {
        conversation.addMessage(assistantMsg);
        contextManager.addMessage(assistantMsg);
        
        for (ToolCall toolCall : assistantMsg.getToolCalls()) {
            ToolCallEvent toolCallEvent = new ToolCallEvent(
                sessionId,
                toolCall.getName(),
                toolCall.getId(),
                toolCall.getArguments()
            );
            emitEvent(toolCallEvent);
            
            try {
                Object result = toolExecutor.executeTool(
                    toolCall.getName(),
                    toolCall.getArguments()
                );
                
                String resultStr = result.toString();
                ToolResultEvent resultEvent = ToolResultEvent.success(
                    sessionId,
                    toolCall.getId(),
                    toolCall.getName(),
                    resultStr
                );
                emitEvent(resultEvent);
                
                ToolMessage toolMsg = new ToolMessage(toolCall.getId(), resultStr);
                conversation.addMessage(toolMsg);
                contextManager.addMessage(toolMsg);
                
            } catch (Exception e) {
                ToolResultEvent errorEvent = ToolResultEvent.failure(
                    sessionId,
                    toolCall.getId(),
                    toolCall.getName(),
                    e.getMessage()
                );
                emitEvent(errorEvent);
            }
        }
        
        ChatRequest followUp = ChatRequestBuilder.create(model)
                .messages(contextManager.compress())
                .tools(toolExecutor)
                .build();
        
        ChatResponse finalResponse = client.chat(followUp);
        AssistantMessage finalMsg = finalResponse.getAssistantMessage();
        
        String content = finalMsg.getContent();
        int tokensUsed = finalResponse.getUsage().getTotalTokens();
        
        AssistantMessageEvent assistantEvent = new AssistantMessageEvent(
            sessionId, content, model, tokensUsed
        );
        emitEvent(assistantEvent);
        
        conversation.addMessage(finalMsg);
        contextManager.addMessage(finalMsg);
        memory.add(finalMsg);
        
        return content;
    }
    
    public void sendMessageStream(String userMessage, StreamCallback streamCallback) throws IOException {
        updateActivity();
        
        UserMessageEvent userEvent = new UserMessageEvent(sessionId, userMessage, userId);
        emitEvent(userEvent);
        
        UserMessage userMsg = new UserMessage(userMessage);
        conversation.addMessage(userMsg);
        contextManager.addMessage(userMsg);
        memory.add(userMsg);
        
        ChatRequest request = ChatRequestBuilder.create(model)
                .messages(contextManager.compress())
                .tools(toolExecutor)
                .build();
        
        final StringBuilder fullContent = new StringBuilder();
        
        client.chatStream(request, new StreamCallback() {
            @Override
            public void onStart() {
                streamCallback.onStart();
            }
            
            @Override
            public void onChunk(String content) {
                fullContent.append(content);
                streamCallback.onChunk(content);
            }
            
            @Override
            public void onComplete(String content) {
                AssistantMessage assistantMsg = new AssistantMessage(fullContent.toString());
                AssistantMessageEvent assistantEvent = new AssistantMessageEvent(
                    sessionId, fullContent.toString(), model, 0
                );
                emitEvent(assistantEvent);
                
                conversation.addMessage(assistantMsg);
                contextManager.addMessage(assistantMsg);
                memory.add(assistantMsg);
                
                streamCallback.onComplete(content);
            }
            
            @Override
            public void onError(Exception error) {
                streamCallback.onError(error);
            }
        });
    }
    
    public void addEventListener(EventHandler handler) {
        eventHandlers.add(handler);
    }
    
    public void removeEventListener(EventHandler handler) {
        eventHandlers.remove(handler);
    }
    
    private void emitEvent(Event event) {
        events.add(event);
        for (EventHandler handler : eventHandlers) {
            handler.onEvent(event);
            
            if (event instanceof UserMessageEvent) {
                handler.onUserMessage((UserMessageEvent) event);
            } else if (event instanceof AssistantMessageEvent) {
                handler.onAssistantMessage((AssistantMessageEvent) event);
            } else if (event instanceof ToolCallEvent) {
                handler.onToolCall((ToolCallEvent) event);
            } else if (event instanceof ToolResultEvent) {
                handler.onToolResult((ToolResultEvent) event);
            } else if (event instanceof ThinkingEvent) {
                handler.onThinking((ThinkingEvent) event);
            }
        }
    }
    
    private void updateActivity() {
        lastActivityAt = Instant.now();
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public List<Event> getEvents() {
        return new ArrayList<>(events);
    }
    
    public Conversation getConversation() {
        return conversation;
    }
    
    public Memory getMemory() {
        return memory;
    }
    
    public ContextManager getContextManager() {
        return contextManager;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getLastActivityAt() {
        return lastActivityAt;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void close() {
        active = false;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String sessionId;
        private String userId;
        private LLMClient client;
        private String model;
        private ToolExecutor toolExecutor;
        private String systemPrompt;
        private Memory memory;
        private ContextManager contextManager;
        
        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }
        
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder client(LLMClient client) {
            this.client = client;
            return this;
        }
        
        public Builder model(String model) {
            this.model = model;
            return this;
        }
        
        public Builder toolExecutor(ToolExecutor toolExecutor) {
            this.toolExecutor = toolExecutor;
            return this;
        }
        
        public Builder systemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }
        
        public Builder memory(Memory memory) {
            this.memory = memory;
            return this;
        }
        
        public Builder contextManager(ContextManager contextManager) {
            this.contextManager = contextManager;
            return this;
        }
        
        public Session build() {
            if (client == null) {
                throw new IllegalStateException("LLMClient is required");
            }
            if (model == null || model.isEmpty()) {
                throw new IllegalStateException("Model is required");
            }
            if (toolExecutor == null) {
                toolExecutor = new ToolExecutor();
            }
            return new Session(this);
        }
    }
}
