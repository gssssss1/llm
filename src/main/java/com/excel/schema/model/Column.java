package com.excel.schema.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Column {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("data_type")
    private DataType dataType;
    
    @JsonProperty("label")
    private String label;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("required")
    private Boolean required;
    
    @JsonProperty("enum_values")
    private List<String> enumValues;
}
