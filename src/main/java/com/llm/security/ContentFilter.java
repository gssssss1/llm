package com.llm.security;

/**
 * Filters content for inappropriate or harmful material.
 */
public interface ContentFilter {

    /**
     * Filters the input text for policy violations.
     *
     * @return filtered text, or original if no violations
     */
    String filter(String text);

    /**
     * Checks if text violates content policy.
     */
    boolean isViolation(String text);
}
