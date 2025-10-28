package com.jsonschema.llm.client;

public interface StreamCallback {
    
    void onStart();
    
    void onChunk(String content);
    
    void onComplete(String fullContent);
    
    void onError(Exception error);
}
