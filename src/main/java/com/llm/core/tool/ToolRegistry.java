package com.llm.core.tool;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of tools available to the session.
 */
public class ToolRegistry {

    private final Map<String, Tool> tools = new ConcurrentHashMap<>();

    public void register(Tool tool) {
        tools.put(tool.getName(), tool);
    }

    public Tool get(String name) {
        return tools.get(name);
    }

    public void unregister(String name) {
        tools.remove(name);
    }

    public Collection<Tool> list() {
        return tools.values();
    }
}
