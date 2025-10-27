package com.llm.core.tool;

import com.llm.exception.LLMException;

import java.util.Map;

/**
 * Validates tool definitions and invocations.
 */
public class ToolValidator {

    public void validate(Tool tool) {
        if (tool.getName() == null || tool.getName().trim().isEmpty()) {
            throw new LLMException("Tool name cannot be empty");
        }

        if (tool.getDescription() == null || tool.getDescription().trim().isEmpty()) {
            throw new LLMException("Tool description cannot be empty");
        }

        if (tool.getParameterSchema() == null) {
            throw new LLMException("Tool parameter schema cannot be null");
        }
    }

    public void validateArguments(Tool tool, Map<String, Object> arguments) {
        Map<String, Object> schema = tool.getParameterSchema();

        if (schema.containsKey("required")) {
            Object requiredObj = schema.get("required");
            if (requiredObj instanceof Iterable) {
                for (Object field : (Iterable<?>) requiredObj) {
                    String fieldName = field.toString();
                    if (!arguments.containsKey(fieldName)) {
                        throw new LLMException(String.format(
                                "Required parameter '%s' missing for tool '%s'",
                                fieldName,
                                tool.getName()
                        ));
                    }
                }
            }
        }
    }
}
