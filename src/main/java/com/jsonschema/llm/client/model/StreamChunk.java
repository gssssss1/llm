package com.jsonschema.llm.client.model;

public class StreamChunk {
    private final String id;
    private final String content;
    private final String finishReason;
    private final boolean isDone;
    
    public StreamChunk(String id, String content, String finishReason, boolean isDone) {
        this.id = id;
        this.content = content;
        this.finishReason = finishReason;
        this.isDone = isDone;
    }
    
    public String getId() {
        return id;
    }
    
    public String getContent() {
        return content;
    }
    
    public String getFinishReason() {
        return finishReason;
    }
    
    public boolean isDone() {
        return isDone;
    }
    
    @Override
    public String toString() {
        return "StreamChunk{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", finishReason='" + finishReason + '\'' +
                ", isDone=" + isDone +
                '}';
    }
}
