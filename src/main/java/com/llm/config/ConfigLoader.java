package com.llm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.llm.exception.LLMException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Loads configuration from YAML files and environment variables.
 */
public class ConfigLoader {

    private final ObjectMapper yamlMapper;

    public ConfigLoader() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }

    /**
     * Loads configuration from a YAML file.
     */
    public Map<String, Object> loadFromFile(String filePath) {
        try {
            return yamlMapper.readValue(new File(filePath), Map.class);
        } catch (IOException e) {
            throw new LLMException("Failed to load configuration from file: " + filePath, e);
        }
    }

    /**
     * Loads configuration from classpath resource.
     */
    public Map<String, Object> loadFromClasspath(String resourcePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new LLMException("Configuration resource not found: " + resourcePath);
            }
            return yamlMapper.readValue(is, Map.class);
        } catch (IOException e) {
            throw new LLMException("Failed to load configuration from classpath: " + resourcePath, e);
        }
    }

    /**
     * Loads configuration from environment variables.
     */
    public String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }
}
