package com.llm.provider;

/**
 * Enumeration of supported provider types.
 */
public enum ProviderType {
    OPENAI("openai"),
    ANTHROPIC("anthropic"),
    AZURE_OPENAI("azure-openai");

    private final String id;

    ProviderType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static ProviderType fromId(String id) {
        for (ProviderType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown provider: " + id);
    }
}
