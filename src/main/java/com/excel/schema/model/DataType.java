package com.excel.schema.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DataType {
    STRING("string"),
    INTEGER("integer"),
    DOUBLE("double"),
    BOOLEAN("boolean"),
    DATE("date"),
    ENUM("enum");
    
    private final String value;
    
    DataType(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
    
    @JsonCreator
    public static DataType fromValue(String value) {
        for (DataType type : DataType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown data type: " + value);
    }
}
