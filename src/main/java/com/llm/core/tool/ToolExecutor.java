package com.llm.core.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes tools called by the LLM.
 */
public class ToolExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ToolExecutor.class);

    private final ToolRegistry registry;

    public ToolExecutor(ToolRegistry registry) {
        this.registry = registry;
    }

    public ToolResult execute(ToolCall toolCall) {
        Tool tool = registry.get(toolCall.getName());
        if (tool == null) {
            logger.warn("Tool not found: {}", toolCall.getName());
            return ToolResult.builder()
                    .content("Tool not found: " + toolCall.getName())
                    .success(false)
                    .build();
        }

        try {
            return tool.execute(toolCall.getArguments());
        } catch (Exception e) {
            logger.error("Error executing tool: " + toolCall.getName(), e);
            return ToolResult.builder()
                    .content("Error: " + e.getMessage())
                    .success(false)
                    .build();
        }
    }
}
