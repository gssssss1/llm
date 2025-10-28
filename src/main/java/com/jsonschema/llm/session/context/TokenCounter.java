package com.jsonschema.llm.session.context;

public interface TokenCounter {
    int count(String text);
}
