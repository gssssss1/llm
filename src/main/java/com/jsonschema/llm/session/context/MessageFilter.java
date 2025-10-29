package com.jsonschema.llm.session.context;

import com.jsonschema.llm.message.Message;
import com.jsonschema.llm.message.MessageRole;

public interface MessageFilter {
    boolean accept(Message message);
    
    static MessageFilter byRole(MessageRole role) {
        return message -> message.getRole() == role;
    }
    
    static MessageFilter byContent(String keyword) {
        return message -> message.getContent() != null && 
                         message.getContent().contains(keyword);
    }
    
    static MessageFilter not(MessageFilter filter) {
        return message -> !filter.accept(message);
    }
    
    static MessageFilter and(MessageFilter... filters) {
        return message -> {
            for (MessageFilter filter : filters) {
                if (!filter.accept(message)) {
                    return false;
                }
            }
            return true;
        };
    }
    
    static MessageFilter or(MessageFilter... filters) {
        return message -> {
            for (MessageFilter filter : filters) {
                if (filter.accept(message)) {
                    return true;
                }
            }
            return false;
        };
    }
}
