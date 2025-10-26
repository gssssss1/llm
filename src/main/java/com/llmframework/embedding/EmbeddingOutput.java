package com.llmframework.embedding;

import com.llmframework.core.model.ModelOutput;
import com.llmframework.core.usage.Usage;

import java.time.Duration;
import java.util.List;

/**
 * Embedding model output
 */
public record EmbeddingOutput(
    String requestId,
    List<List<Float>> embeddings,
    Usage usage,
    String modelVersion,
    Duration duration,
    boolean fromCache
) implements ModelOutput {
    
    public List<Float> getFirst() {
        return embeddings.isEmpty() ? List.of() : embeddings.get(0);
    }
    
    public int dimensions() {
        return embeddings.isEmpty() ? 0 : embeddings.get(0).size();
    }
}
