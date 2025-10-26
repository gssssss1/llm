package com.llmframework.tool;

import com.llmframework.format.JsonSchema;

import java.util.Map;
import java.util.function.Function;

/**
 * Tool/Function definition
 */
public record Tool(
    String name,
    String description,
    JsonSchema parameters,
    Function<Map<String, Object>, Object> function
) {
    
    public static Builder builder(String name) {
        return new Builder(name);
    }
    
    public static class Builder {
        private final String name;
        private String description;
        private JsonSchema parameters = JsonSchema.object();
        private Function<Map<String, Object>, Object> function;
        
        private Builder(String name) {
            this.name = name;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder parameters(JsonSchema parameters) {
            this.parameters = parameters;
            return this;
        }
        
        public Builder parameter(String name, Class<?> type, String description) {
            this.parameters.addProperty(name, type, description);
            return this;
        }
        
        public Builder parameter(String name, Class<?> type, String description, boolean required) {
            this.parameters.addProperty(name, type, description, required);
            return this;
        }
        
        public Builder function(Function<Map<String, Object>, Object> function) {
            this.function = function;
            return this;
        }
        
        public Tool build() {
            return new Tool(name, description, parameters, function);
        }
    }
}
