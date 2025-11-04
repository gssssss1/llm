package com.excel.schema.generator;

import com.excel.schema.model.ExcelSchema;

public class ReportGeneratorFactory {
    
    public enum FileType {
        EXCEL("excel"),
        CSV("csv");
        
        private final String value;
        
        FileType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static FileType fromString(String value) {
            for (FileType type : FileType.values()) {
                if (type.value.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown file type: " + value);
        }
    }
    
    public static Object createGenerator(ExcelSchema schema) {
        if (schema.getReportFileType() == null) {
            throw new IllegalArgumentException("Report file type is not specified");
        }
        
        FileType fileType = FileType.fromString(schema.getReportFileType());
        
        switch (fileType) {
            case EXCEL:
                return new ExcelGenerator(schema);
            case CSV:
                return new CsvGenerator(schema);
            default:
                throw new IllegalArgumentException("Unsupported file type: " + schema.getReportFileType());
        }
    }
    
    public static Object createDataPopulator(ExcelSchema schema, Object generator) {
        if (schema.getReportFileType() == null) {
            throw new IllegalArgumentException("Report file type is not specified");
        }
        
        FileType fileType = FileType.fromString(schema.getReportFileType());
        
        switch (fileType) {
            case EXCEL:
                if (generator instanceof org.apache.poi.ss.usermodel.Workbook) {
                    return new ExcelDataPopulator(schema, (org.apache.poi.ss.usermodel.Workbook) generator);
                }
                throw new IllegalArgumentException("Invalid generator type for Excel");
            case CSV:
                return new CsvDataPopulator(schema);
            default:
                throw new IllegalArgumentException("Unsupported file type: " + schema.getReportFileType());
        }
    }
}
