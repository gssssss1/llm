package com.llmframework.core.model;

import java.util.List;
import java.util.Set;

/**
 * Model capabilities interface
 */
public interface ModelCapabilities {
    
    /**
     * Check if streaming is supported
     */
    boolean supportsStreaming();
    
    /**
     * Check if function calling is supported
     */
    boolean supportsFunctionCalling();
    
    /**
     * Check if vision (image input) is supported
     */
    boolean supportsVision();
    
    /**
     * Check if audio is supported
     */
    boolean supportsAudio();
    
    /**
     * Check if JSON mode is supported
     */
    boolean supportsJsonMode();
    
    /**
     * Check if JSON Schema is supported
     */
    boolean supportsJsonSchema();
    
    /**
     * Get max context tokens
     */
    int getMaxContextTokens();
    
    /**
     * Get max output tokens
     */
    int getMaxOutputTokens();
    
    /**
     * Get supported languages
     */
    List<String> getSupportedLanguages();
    
    /**
     * Get supported features
     */
    Set<String> getSupportedFeatures();
    
    /**
     * Check if a specific feature is supported
     */
    default boolean supports(String feature) {
        return getSupportedFeatures().contains(feature);
    }
}
