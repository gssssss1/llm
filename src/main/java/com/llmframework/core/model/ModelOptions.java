package com.llmframework.core.model;

import java.util.*;

/**
 * Model options - immutable and type-safe
 */
public record ModelOptions(
    Double temperature,
    Double topP,
    Integer maxTokens,
    List<String> stop,
    Double presencePenalty,
    Double frequencyPenalty,
    Integer seed,
    String user,
    Map<String, Object> extra
) {
    
    public ModelOptions {
        temperature = clamp(temperature, 0.0, 2.0);
        topP = clamp(topP, 0.0, 1.0);
        maxTokens = maxTokens == null ? null : Math.max(1, maxTokens);
        presencePenalty = clamp(presencePenalty, -2.0, 2.0);
        frequencyPenalty = clamp(frequencyPenalty, -2.0, 2.0);
        stop = stop == null ? List.of() : List.copyOf(stop);
        extra = extra == null ? Map.of() : Map.copyOf(extra);
    }
    
    private static Double clamp(Double value, double min, double max) {
        if (value == null) return null;
        return Math.max(min, Math.min(max, value));
    }
    
    public static ModelOptions defaults() {
        return builder().build();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Double temperature;
        private Double topP;
        private Integer maxTokens = 1024;
        private List<String> stop = new ArrayList<>();
        private Double presencePenalty;
        private Double frequencyPenalty;
        private Integer seed;
        private String user;
        private Map<String, Object> extra = new HashMap<>();
        
        public Builder temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }
        
        public Builder topP(double topP) {
            this.topP = topP;
            return this;
        }
        
        public Builder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }
        
        public Builder stop(String... stop) {
            this.stop.addAll(Arrays.asList(stop));
            return this;
        }
        
        public Builder presencePenalty(double presencePenalty) {
            this.presencePenalty = presencePenalty;
            return this;
        }
        
        public Builder frequencyPenalty(double frequencyPenalty) {
            this.frequencyPenalty = frequencyPenalty;
            return this;
        }
        
        public Builder seed(int seed) {
            this.seed = seed;
            return this;
        }
        
        public Builder user(String user) {
            this.user = user;
            return this;
        }
        
        public Builder extra(String key, Object value) {
            this.extra.put(key, value);
            return this;
        }
        
        public ModelOptions build() {
            return new ModelOptions(
                temperature, topP, maxTokens, stop,
                presencePenalty, frequencyPenalty, seed, user, extra
            );
        }
    }
    
    // Common presets
    public static ModelOptions creative() {
        return builder().temperature(0.9).topP(0.95).build();
    }
    
    public static ModelOptions precise() {
        return builder().temperature(0.1).topP(0.1).build();
    }
    
    public static ModelOptions balanced() {
        return builder().temperature(0.7).topP(0.9).build();
    }
}
