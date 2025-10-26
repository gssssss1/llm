package com.llmframework.image;

import com.llmframework.core.model.ModelInput;
import com.llmframework.core.model.ModelOptions;
import com.llmframework.core.usage.TokenCounter;
import com.llmframework.core.usage.TokenEstimate;

import java.util.*;

/**
 * Image model input
 */
public record ImageInput(
    String prompt,
    String negativePrompt,
    String image,
    String mask,
    Integer n,
    String size,
    String quality,
    String style,
    ModelOptions options,
    String requestId,
    Map<String, String> metadata
) implements ModelInput {
    
    public ImageInput {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        requestId = requestId == null ? UUID.randomUUID().toString() : requestId;
    }
    
    public static ImageInput of(String prompt) {
        return builder().prompt(prompt).build();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @Override
    public TokenEstimate estimateTokens() {
        int tokens = prompt != null ? TokenCounter.estimate(prompt) : 0;
        return new TokenEstimate(tokens, 0);
    }
    
    public static class Builder {
        private String prompt;
        private String negativePrompt;
        private String image;
        private String mask;
        private Integer n = 1;
        private String size = "1024x1024";
        private String quality = "standard";
        private String style;
        private ModelOptions options = ModelOptions.defaults();
        private String requestId;
        private Map<String, String> metadata = new HashMap<>();
        
        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }
        
        public Builder negativePrompt(String negativePrompt) {
            this.negativePrompt = negativePrompt;
            return this;
        }
        
        public Builder image(String image) {
            this.image = image;
            return this;
        }
        
        public Builder mask(String mask) {
            this.mask = mask;
            return this;
        }
        
        public Builder n(int n) {
            this.n = n;
            return this;
        }
        
        public Builder size(String size) {
            this.size = size;
            return this;
        }
        
        public Builder size(int width, int height) {
            this.size = width + "x" + height;
            return this;
        }
        
        public Builder quality(String quality) {
            this.quality = quality;
            return this;
        }
        
        public Builder style(String style) {
            this.style = style;
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
        
        public ImageInput build() {
            return new ImageInput(
                prompt, negativePrompt, image, mask, n, size,
                quality, style, options, requestId, metadata
            );
        }
    }
}
