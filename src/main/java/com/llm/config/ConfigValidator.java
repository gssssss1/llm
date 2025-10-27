package com.llm.config;

import com.llm.exception.LLMException;

import java.util.Map;

/**
 * Validates session configuration.
 */
public class ConfigValidator {

    public void validate(SessionConfig config) {
        if (config == null) {
            throw new LLMException("SessionConfig cannot be null");
        }
        if (config.getModelConfig() == null) {
            throw new LLMException("Model configuration is required");
        }
        if (config.getModelConfig().getProvider() == null) {
            throw new LLMException("Provider is required");
        }
    }

    public void validate(Map<String, Object> rawConfig) {
        if (!rawConfig.containsKey("provider")) {
            throw new LLMException("provider section missing in configuration");
        }
    }
}
