package com.llm.security;

import java.util.Arrays;
import java.util.List;

/**
 * Basic content filter implementation using keyword matching.
 */
public class BasicContentFilter implements ContentFilter {

    private static final List<String> BANNED_WORDS = Arrays.asList("violence", "self-harm");

    @Override
    public String filter(String text) {
        if (text == null) {
            return null;
        }
        String filtered = text;
        for (String word : BANNED_WORDS) {
            filtered = filtered.replaceAll("(?i)" + word, "***");
        }
        return filtered;
    }

    @Override
    public boolean isViolation(String text) {
        if (text == null) {
            return false;
        }
        return BANNED_WORDS.stream().anyMatch(word -> text.toLowerCase().contains(word.toLowerCase()));
    }
}
