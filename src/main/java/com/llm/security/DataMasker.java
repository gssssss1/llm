package com.llm.security;

import java.util.regex.Pattern;

/**
 * Masks sensitive data such as emails, phone numbers, and credit cards.
 */
public class DataMasker {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b"
    );

    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile(
            "\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b"
    );

    public String mask(String text) {
        if (text == null) {
            return null;
        }

        String masked = text;
        masked = EMAIL_PATTERN.matcher(masked).replaceAll("[EMAIL]");
        masked = PHONE_PATTERN.matcher(masked).replaceAll("[PHONE]");
        masked = CREDIT_CARD_PATTERN.matcher(masked).replaceAll("[CARD]");

        return masked;
    }
}
