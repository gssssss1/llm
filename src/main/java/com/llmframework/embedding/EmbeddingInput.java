package com.llmframework.embedding;

import com.llmframework.core.model.ModelInput;
import com.llmframework.core.model.ModelOptions;
import com.llmframework.core.usage.TokenCounter;
import com.llmframework.core.usage.TokenEstimate;

import java.util.*;

/**
 * Embedding model input
 */
public record EmbeddingInput(
    List<String> texts,
    Integer dimensions,
    ModelOptions options,
    String requestId,
    Map<String, String> metadata
) implements ModelInput {
    
    public EmbeddingInput {
        texts = List.copyOf(texts);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        requestId = requestId == null ? UUID.randomUUID().toString() : requestId;
    }
    
    public static EmbeddingInput of(String text) {
        return builder().text(text).build();
    }
    
    public static EmbeddingInput of(List<String> texts) {
        return builder().texts(texts).build();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @Override
    public TokenEstimate estimateTokens() {
        int totalTokens = texts.stream()
            .mapToInt(TokenCounter::estimate)
            .sum();
        return new TokenEstimate(totalTokens, 0);
    }
    
    public static class Builder {
        private List<String> texts = new ArrayList<>();
        private Integer dimensions;
        private ModelOptions options = ModelOptions.defaults();
        private String requestId;
        private Map<String, String> metadata = new HashMap<>();
        
        public Builder text(String text) {
            this.texts.add(text);
            return this;
        }
        
        public Builder texts(List<String> texts) {
            this.texts.addAll(texts);
            return this;
        }
        
        public Builder dimensions(int dimensions) {
            this.dimensions = dimensions;
            return this;
        }
        
        public Builder options(ModelOptions options) {
            this.options = options;
            return this;
        }
        
        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }
        
        public Builder metadata(String key, String value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public EmbeddingInput build() {
            return new EmbeddingInput(texts, dimensions, options, requestId, metadata);
        }
    }
}
