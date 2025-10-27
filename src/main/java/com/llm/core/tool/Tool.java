package com.llm.core.tool;

import java.util.Map;

/**
 * Represents a tool that can be called by the LLM.
 */
public interface Tool {

    /**
     * Returns the unique name of the tool.
     */
    String getName();

    /**
     * Returns a description of what the tool does.
     */
    String getDescription();

    /**
     * Returns the tool's parameter schema.
     */
    Map<String, Object> getParameterSchema();

    /**
     * Executes the tool with the provided arguments.
     */
    ToolResult execute(Map<String, Object> arguments);
}
