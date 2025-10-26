package com.llmframework.core.model;

import com.llmframework.cost.PricingInfo;

/**
 * Model metadata information
 */
public record ModelMetadata(
    String name,
    String version,
    String provider,
    ModelType type,
    ModelCapabilities capabilities,
    PricingInfo pricing
) {
    
    public static Builder builder(String name) {
        return new Builder(name);
    }
    
    public static class Builder {
        private final String name;
        private String version = "latest";
        private String provider;
        private ModelType type = ModelType.CHAT;
        private ModelCapabilities capabilities;
        private PricingInfo pricing;
        
        private Builder(String name) {
            this.name = name;
        }
        
        public Builder version(String version) {
            this.version = version;
            return this;
        }
        
        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }
        
        public Builder type(ModelType type) {
            this.type = type;
            return this;
        }
        
        public Builder capabilities(ModelCapabilities capabilities) {
            this.capabilities = capabilities;
            return this;
        }
        
        public Builder pricing(PricingInfo pricing) {
            this.pricing = pricing;
            return this;
        }
        
        public ModelMetadata build() {
            return new ModelMetadata(name, version, provider, type, capabilities, pricing);
        }
    }
}
