package com.llmframework.tool;

/**
 * Tool choice strategy
 */
public sealed interface ToolChoice permits AutoToolChoice, RequiredToolChoice, SpecificToolChoice, NoneToolChoice {
    
    static ToolChoice auto() {
        return AutoToolChoice.INSTANCE;
    }
    
    static ToolChoice required() {
        return RequiredToolChoice.INSTANCE;
    }
    
    static ToolChoice specific(String toolName) {
        return new SpecificToolChoice(toolName);
    }
    
    static ToolChoice none() {
        return NoneToolChoice.INSTANCE;
    }
}

final class AutoToolChoice implements ToolChoice {
    public static final AutoToolChoice INSTANCE = new AutoToolChoice();
    private AutoToolChoice() {}
}

final class RequiredToolChoice implements ToolChoice {
    public static final RequiredToolChoice INSTANCE = new RequiredToolChoice();
    private RequiredToolChoice() {}
}

record SpecificToolChoice(String toolName) implements ToolChoice {}

final class NoneToolChoice implements ToolChoice {
    public static final NoneToolChoice INSTANCE = new NoneToolChoice();
    private NoneToolChoice() {}
}
