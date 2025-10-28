package com.jsonschema.llm.session.memory;

import com.jsonschema.llm.message.Message;

import java.util.List;

public interface Memory {
    
    void add(Message message);
    
    void add(String key, String value);
    
    String get(String key);
    
    List<Message> getMessages();
    
    List<Message> getRecentMessages(int count);
    
    void clear();
    
    int size();
}
