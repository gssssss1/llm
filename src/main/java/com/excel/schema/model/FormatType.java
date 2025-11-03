package com.excel.schema.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FormatType {
    KEY_VALUE("key_value"),
    TABULAR("tabular");
    
    private final String value;
    
    FormatType(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
    
    @JsonCreator
    public static FormatType fromValue(String value) {
        for (FormatType type : FormatType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown format type: " + value);
    }
}
