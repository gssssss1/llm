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
public class ExcelSchema {
    
    @JsonProperty("report_name")
    private String reportName;
    
    @JsonProperty("report_file_type")
    private String reportFileType;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("schema")
    private Schema schema;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Schema {
        @JsonProperty("sheet")
        private List<Sheet> sheets;
    }
}
