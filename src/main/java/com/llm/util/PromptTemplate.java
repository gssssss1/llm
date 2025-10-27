package com.llm.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Template engine for creating prompts with variable substitution.
 */
public class PromptTemplate {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(\\w+)}}");
    private final String template;

    private PromptTemplate(String template) {
        this.template = template;
    }

    public static PromptTemplate from(String template) {
        return new PromptTemplate(template);
    }

    public String render(Map<String, Object> variables) {
        StringBuilder result = new StringBuilder();
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        int lastEnd = 0;

        while (matcher.find()) {
            result.append(template, lastEnd, matcher.start());
            String varName = matcher.group(1);
            Object value = variables.get(varName);
            result.append(value != null ? value : "{{" + varName + "}}");
            lastEnd = matcher.end();
        }
        result.append(template.substring(lastEnd));
        return result.toString();
    }

    public static String quickRender(String template, Object... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Args must be key-value pairs");
        }

        String result = template;
        for (int i = 0; i < args.length; i += 2) {
            String key = args[i].toString();
            String value = args[i + 1].toString();
            result = result.replaceAll("\\{\\{" + key + "}}", Matcher.quoteReplacement(value));
        }
        return result;
    }
}
