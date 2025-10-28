package com.jsonschema.llm.client;

import com.jsonschema.llm.client.model.ChatRequest;
import com.jsonschema.llm.client.model.ChatResponse;

import java.io.IOException;

public interface LLMClient {
    
    ChatResponse chat(ChatRequest request) throws IOException;
    
    void close();
}
