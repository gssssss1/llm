package com.llm.core.session;

import com.llm.config.ExecutionConfig;
import com.llm.config.FeatureFlags;
import com.llm.config.ModelConfig;
import com.llm.config.SecurityConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration for an LLM session.
 */
public class SessionConfig {

    private final ModelConfig modelConfig;
    private final ExecutionConfig executionConfig;
    private final FeatureFlags featureFlags;
    private final SecurityConfig securityConfig;
    private final Map<String, Object> customProperties;

    private SessionConfig(Builder builder) {
        this.modelConfig = Objects.requireNonNull(builder.modelConfig, "modelConfig is required");
        this.executionConfig = builder.executionConfig != null ? builder.executionConfig : ExecutionConfig.defaults();
        this.featureFlags = builder.featureFlags != null ? builder.featureFlags : FeatureFlags.defaults();
        this.securityConfig = builder.securityConfig != null ? builder.securityConfig : SecurityConfig.defaults();
        this.customProperties = new HashMap<>(builder.customProperties);
    }

    public ModelConfig getModelConfig() {
        return modelConfig;
    }

    public ExecutionConfig getExecutionConfig() {
        return executionConfig;
    }

    public FeatureFlags getFeatureFlags() {
        return featureFlags;
    }

    public SecurityConfig getSecurityConfig() {
        return securityConfig;
    }

    public Map<String, Object> getCustomProperties() {
        return new HashMap<>(customProperties);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for session configuration.
     */
    public static class Builder {
        private ModelConfig modelConfig;
        private ExecutionConfig executionConfig;
        private FeatureFlags featureFlags;
        private SecurityConfig securityConfig;
        private Map<String, Object> customProperties = new HashMap<>();

        public Builder modelConfig(ModelConfig modelConfig) {
            this.modelConfig = modelConfig;
            return this;
        }

        public Builder executionConfig(ExecutionConfig executionConfig) {
            this.executionConfig = executionConfig;
            return this;
        }

        public Builder featureFlags(FeatureFlags featureFlags) {
            this.featureFlags = featureFlags;
            return this;
        }

        public Builder securityConfig(SecurityConfig securityConfig) {
            this.securityConfig = securityConfig;
            return this;
        }

        public Builder customProperty(String key, Object value) {
            this.customProperties.put(key, value);
            return this;
        }

        public SessionConfig build() {
            return new SessionConfig(this);
        }
    }
}
