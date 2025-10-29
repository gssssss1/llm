package com.jsonschema.llm.session.memory;

import com.jsonschema.llm.message.Message;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShortTermMemory implements Memory {
    private final List<Message> messages;
    private final Map<String, String> keyValueStore;
    private final int maxSize;
    
    public ShortTermMemory() {
        this(100);
    }
    
    public ShortTermMemory(int maxSize) {
        this.messages = Collections.synchronizedList(new ArrayList<>());
        this.keyValueStore = new ConcurrentHashMap<>();
        this.maxSize = maxSize;
    }
    
    @Override
    public void add(Message message) {
        messages.add(message);
        if (messages.size() > maxSize) {
            messages.remove(0);
        }
    }
    
    @Override
    public void add(String key, String value) {
        keyValueStore.put(key, value);
    }
    
    @Override
    public String get(String key) {
        return keyValueStore.get(key);
    }
    
    @Override
    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }
    
    @Override
    public List<Message> getRecentMessages(int count) {
        int size = messages.size();
        int fromIndex = Math.max(0, size - count);
        return new ArrayList<>(messages.subList(fromIndex, size));
    }
    
    @Override
    public void clear() {
        messages.clear();
        keyValueStore.clear();
    }
    
    @Override
    public int size() {
        return messages.size();
    }
}
