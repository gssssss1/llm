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
public class Sheet {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("format_type")
    private FormatType formatType;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("columns")
    private List<Column> columns;
}
