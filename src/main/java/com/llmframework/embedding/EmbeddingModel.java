package com.llmframework.embedding;

import com.llmframework.core.model.ReactiveModel;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Embedding model interface
 */
public interface EmbeddingModel extends ReactiveModel<EmbeddingInput, EmbeddingOutput> {
    
    /**
     * Embed single text (convenience method)
     */
    default Mono<List<Float>> embed(String text) {
        return call(EmbeddingInput.of(text))
            .map(output -> output.embeddings().get(0));
    }
    
    /**
     * Embed multiple texts
     */
    default Mono<List<List<Float>>> embedBatch(List<String> texts) {
        return call(EmbeddingInput.of(texts))
            .map(EmbeddingOutput::embeddings);
    }
}
