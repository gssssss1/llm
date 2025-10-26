package com.llmframework.core.model;

import java.util.*;

/**
 * Default implementation of model capabilities
 */
public record DefaultModelCapabilities(
    boolean supportsStreaming,
    boolean supportsFunctionCalling,
    boolean supportsVision,
    boolean supportsAudio,
    boolean supportsJsonMode,
    boolean supportsJsonSchema,
    int maxContextTokens,
    int maxOutputTokens,
    List<String> supportedLanguages,
    Set<String> supportedFeatures
) implements ModelCapabilities {
    
    @Override
    public List<String> getSupportedLanguages() {
        return supportedLanguages;
    }
    
    @Override
    public Set<String> getSupportedFeatures() {
        return supportedFeatures;
    }
    
    @Override
    public int getMaxContextTokens() {
        return maxContextTokens;
    }
    
    @Override
    public int getMaxOutputTokens() {
        return maxOutputTokens;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private boolean supportsStreaming = true;
        private boolean supportsFunctionCalling = false;
        private boolean supportsVision = false;
        private boolean supportsAudio = false;
        private boolean supportsJsonMode = false;
        private boolean supportsJsonSchema = false;
        private int maxContextTokens = 4096;
        private int maxOutputTokens = 4096;
        private List<String> supportedLanguages = List.of("en");
        private Set<String> supportedFeatures = new HashSet<>();
        
        public Builder supportsStreaming(boolean supports) {
            this.supportsStreaming = supports;
            return this;
        }
        
        public Builder supportsFunctionCalling(boolean supports) {
            this.supportsFunctionCalling = supports;
            if (supports) {
                supportedFeatures.add("function_calling");
            }
            return this;
        }
        
        public Builder supportsVision(boolean supports) {
            this.supportsVision = supports;
            if (supports) {
                supportedFeatures.add("vision");
            }
            return this;
        }
        
        public Builder supportsAudio(boolean supports) {
            this.supportsAudio = supports;
            if (supports) {
                supportedFeatures.add("audio");
            }
            return this;
        }
        
        public Builder supportsJsonMode(boolean supports) {
            this.supportsJsonMode = supports;
            if (supports) {
                supportedFeatures.add("json_mode");
            }
            return this;
        }
        
        public Builder supportsJsonSchema(boolean supports) {
            this.supportsJsonSchema = supports;
            if (supports) {
                supportedFeatures.add("json_schema");
            }
            return this;
        }
        
        public Builder maxContextTokens(int tokens) {
            this.maxContextTokens = tokens;
            return this;
        }
        
        public Builder maxOutputTokens(int tokens) {
            this.maxOutputTokens = tokens;
            return this;
        }
        
        public Builder supportedLanguages(String... languages) {
            this.supportedLanguages = Arrays.asList(languages);
            return this;
        }
        
        public Builder feature(String feature) {
            this.supportedFeatures.add(feature);
            return this;
        }
        
        public DefaultModelCapabilities build() {
            return new DefaultModelCapabilities(
                supportsStreaming, supportsFunctionCalling, supportsVision, supportsAudio,
                supportsJsonMode, supportsJsonSchema, maxContextTokens, maxOutputTokens,
                supportedLanguages, Set.copyOf(supportedFeatures)
            );
        }
    }
}
