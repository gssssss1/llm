package com.langgraph.state;

import java.time.Instant;
import java.util.Map;

public sealed interface State permits StateRecord {
    String id();
    Instant timestamp();
    Map<String, Object> data();
    
    default <T> T get(String key, Class<T> type) {
        Object value = data().get(key);
        if (value == null) {
            return null;
        }
        return type.cast(value);
    }
    
    default Object get(String key) {
        return data().get(key);
    }
}
