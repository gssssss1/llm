package com.llmframework.format;

/**
 * Response format specification
 */
public sealed interface ResponseFormat permits TextFormat, JsonFormat, JsonSchemaFormat {
    
    FormatType type();
    
    static ResponseFormat text() {
        return TextFormat.INSTANCE;
    }
    
    static ResponseFormat json() {
        return JsonFormat.INSTANCE;
    }
    
    static <T> ResponseFormat jsonSchema(Class<T> type) {
        return new JsonSchemaFormat(JsonSchemaGenerator.generate(type), type);
    }
    
    static ResponseFormat jsonSchema(String schema) {
        return new JsonSchemaFormat(schema, null);
    }
}

enum FormatType {
    TEXT,
    JSON,
    JSON_SCHEMA
}

final class TextFormat implements ResponseFormat {
    public static final TextFormat INSTANCE = new TextFormat();
    private TextFormat() {}
    
    @Override
    public FormatType type() {
        return FormatType.TEXT;
    }
}

final class JsonFormat implements ResponseFormat {
    public static final JsonFormat INSTANCE = new JsonFormat();
    private JsonFormat() {}
    
    @Override
    public FormatType type() {
        return FormatType.JSON;
    }
}

record JsonSchemaFormat(
    String schema,
    Class<?> targetType
) implements ResponseFormat {
    
    @Override
    public FormatType type() {
        return FormatType.JSON_SCHEMA;
    }
    
    public boolean hasTargetType() {
        return targetType != null;
    }
}
