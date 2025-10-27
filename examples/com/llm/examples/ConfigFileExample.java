package com.llm.examples;

import com.llm.config.ConfigLoader;
import com.llm.config.ModelConfig;
import com.llm.core.message.UserMessage;
import com.llm.core.session.LLMSession;
import com.llm.core.session.SessionConfig;
import com.llm.core.session.SessionFactory;

import java.util.Map;

/**
 * Demonstrates loading configuration from YAML files.
 */
public class ConfigFileExample {

    public static void main(String[] args) {
        SessionFactory factory = new SessionFactory();
        ConfigLoader loader = new ConfigLoader();

        try {
            // Load configuration from file
            Map<String, Object> rawConfig = loader.loadFromClasspath("llm-default.yml");
            
            // Build config from loaded values
            Map<String, Object> providerMap = (Map<String, Object>) rawConfig.get("provider");
            String provider = (String) providerMap.get("type");
            String model = (String) providerMap.get("model");

            ModelConfig modelConfig = ModelConfig.builder()
                    .provider(provider)
                    .model(model)
                    .build();

            SessionConfig sessionConfig = SessionConfig.builder()
                    .modelConfig(modelConfig)
                    .build();

            // Create session from config
            try (LLMSession session = factory.fromConfig(sessionConfig)) {
                session.send(UserMessage.of("Hello from config file!"));
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
