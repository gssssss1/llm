package com.jsonschema.llm.session.context;

import com.jsonschema.llm.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ContextManager {
    private final List<Message> messages;
    private final int maxTokens;
    private final TokenCounter tokenCounter;
    
    public ContextManager(int maxTokens) {
        this.messages = new ArrayList<>();
        this.maxTokens = maxTokens;
        this.tokenCounter = new SimpleTokenCounter();
    }
    
    public ContextManager(int maxTokens, TokenCounter tokenCounter) {
        this.messages = new ArrayList<>();
        this.maxTokens = maxTokens;
        this.tokenCounter = tokenCounter;
    }
    
    public void addMessage(Message message) {
        messages.add(message);
        ensureWithinLimit();
    }
    
    public void addMessages(List<Message> newMessages) {
        messages.addAll(newMessages);
        ensureWithinLimit();
    }
    
    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }
    
    public List<Message> query(MessageFilter filter) {
        return messages.stream()
                .filter(filter::accept)
                .collect(Collectors.toList());
    }
    
    public void deleteMessage(int index) {
        if (index >= 0 && index < messages.size()) {
            messages.remove(index);
        }
    }
    
    public void deleteMessages(MessageFilter filter) {
        messages.removeIf(filter::accept);
    }
    
    public void clear() {
        messages.clear();
    }
    
    public List<Message> compress() {
        if (getTotalTokens() <= maxTokens) {
            return new ArrayList<>(messages);
        }
        
        List<Message> compressed = new ArrayList<>();
        int currentTokens = 0;
        
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message msg = messages.get(i);
            int msgTokens = tokenCounter.count(msg.getContent());
            
            if (currentTokens + msgTokens <= maxTokens) {
                compressed.add(0, msg);
                currentTokens += msgTokens;
            } else {
                break;
            }
        }
        
        return compressed;
    }
    
    public int getTotalTokens() {
        return messages.stream()
                .mapToInt(msg -> tokenCounter.count(msg.getContent()))
                .sum();
    }
    
    public int size() {
        return messages.size();
    }
    
    public boolean isEmpty() {
        return messages.isEmpty();
    }
    
    private void ensureWithinLimit() {
        while (getTotalTokens() > maxTokens && !messages.isEmpty()) {
            messages.remove(0);
        }
    }
}
