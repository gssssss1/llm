package com.llmframework.core.model;

import com.llmframework.chat.ChatInput;
import com.llmframework.embedding.EmbeddingInput;
import com.llmframework.image.ImageInput;
import com.llmframework.core.usage.TokenEstimate;

import java.util.Map;

/**
 * Base interface for all model inputs
 */
public sealed interface ModelInput
    permits ChatInput, EmbeddingInput, ImageInput {
    
    /**
     * Get request ID (auto-generated or specified)
     */
    String requestId();
    
    /**
     * Get model options
     */
    ModelOptions options();
    
    /**
     * Get metadata for tracking and logging
     */
    Map<String, String> metadata();
    
    /**
     * Estimate token count
     */
    TokenEstimate estimateTokens();
}
