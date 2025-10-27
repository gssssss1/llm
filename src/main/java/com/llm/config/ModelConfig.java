package com.llm.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Model configuration encapsulating provider, model name and parameters.
 */
public class ModelConfig {

    private final String provider;
    private final String model;
    private final Map<String, Object> parameters;

    private ModelConfig(Builder builder) {
        this.provider = Objects.requireNonNull(builder.provider, "provider is required");
        this.model = Objects.requireNonNull(builder.model, "model is required");
        this.parameters = Collections.unmodifiableMap(new HashMap<>(builder.parameters));
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(ModelConfig config) {
        return new Builder()
                .provider(config.provider)
                .model(config.model)
                .parameters(config.parameters);
    }

    /**
     * Builder for model configuration.
     */
    public static class Builder {
        private String provider;
        private String model;
        private Map<String, Object> parameters = new HashMap<>();

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder parameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        public Builder parameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }

        public ModelConfig build() {
            return new ModelConfig(this);
        }
    }
}
