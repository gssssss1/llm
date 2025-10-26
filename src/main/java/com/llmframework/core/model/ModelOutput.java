package com.llmframework.core.model;

import com.llmframework.chat.ChatOutput;
import com.llmframework.embedding.EmbeddingOutput;
import com.llmframework.image.ImageOutput;
import com.llmframework.core.usage.Usage;

import java.time.Duration;

/**
 * Base interface for all model outputs
 */
public sealed interface ModelOutput
    permits ChatOutput, EmbeddingOutput, ImageOutput {
    
    /**
     * Get request ID
     */
    String requestId();
    
    /**
     * Get usage statistics
     */
    Usage usage();
    
    /**
     * Get model version
     */
    String modelVersion();
    
    /**
     * Get duration
     */
    Duration duration();
    
    /**
     * Check if from cache
     */
    boolean fromCache();
}
