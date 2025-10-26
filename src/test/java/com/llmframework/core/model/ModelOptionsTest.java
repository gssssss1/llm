package com.llmframework.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModelOptionsTest {
    
    @Test
    void testDefaultOptions() {
        ModelOptions options = ModelOptions.defaults();
        assertNotNull(options);
        assertEquals(1024, options.maxTokens());
    }
    
    @Test
    void testBuilderPattern() {
        ModelOptions options = ModelOptions.builder()
            .temperature(0.7)
            .maxTokens(500)
            .topP(0.9)
            .build();
        
        assertEquals(0.7, options.temperature());
        assertEquals(500, options.maxTokens());
        assertEquals(0.9, options.topP());
    }
    
    @Test
    void testTemperatureClamping() {
        ModelOptions options = ModelOptions.builder()
            .temperature(3.0) // Should be clamped to 2.0
            .build();
        
        assertEquals(2.0, options.temperature());
    }
    
    @Test
    void testPresets() {
        ModelOptions creative = ModelOptions.creative();
        assertEquals(0.9, creative.temperature());
        
        ModelOptions precise = ModelOptions.precise();
        assertEquals(0.1, precise.temperature());
        
        ModelOptions balanced = ModelOptions.balanced();
        assertEquals(0.7, balanced.temperature());
    }
}
