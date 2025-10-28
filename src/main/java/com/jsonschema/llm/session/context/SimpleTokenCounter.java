package com.jsonschema.llm.session.context;

public class SimpleTokenCounter implements TokenCounter {
    
    @Override
    public int count(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return (int) Math.ceil(text.length() / 4.0);
    }
}
