package com.excel.schema.generator;

import com.excel.schema.model.Column;
import com.excel.schema.model.ExcelSchema;
import com.excel.schema.model.FormatType;
import com.excel.schema.model.Sheet;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class CsvGenerator {
    
    private final ExcelSchema schema;
    private final String separator;
    private final boolean includeHeaders;
    
    public CsvGenerator(ExcelSchema schema) {
        this(schema, ",", true);
    }
    
    public CsvGenerator(ExcelSchema schema, String separator, boolean includeHeaders) {
        this.schema = schema;
        this.separator = separator;
        this.includeHeaders = includeHeaders;
    }
    
    public void generateToDirectory(String directoryPath) throws IOException {
        Path dir = Paths.get(directoryPath);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        
        if (schema.getSchema() == null || schema.getSchema().getSheets() == null) {
            return;
        }
        
        for (Sheet sheet : schema.getSchema().getSheets()) {
            String fileName = sanitizeFileName(sheet.getName()) + ".csv";
            Path filePath = dir.resolve(fileName);
            generateSheet(sheet, filePath.toString());
        }
    }
    
    public void generateSheet(Sheet sheet, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            
            if (sheet.getFormatType() == FormatType.KEY_VALUE) {
                generateKeyValueCsv(writer, sheet);
            } else if (sheet.getFormatType() == FormatType.TABULAR) {
                generateTabularCsv(writer, sheet);
            }
        }
    }
    
    private void generateKeyValueCsv(BufferedWriter writer, Sheet sheet) throws IOException {
        List<Column> columns = sheet.getColumns();
        if (columns == null || columns.isEmpty()) {
            return;
        }
        
        if (includeHeaders) {
            writer.write(escapeCsv("Key") + separator + escapeCsv("Value"));
            writer.newLine();
        }
        
        for (Column column : columns) {
            writer.write(escapeCsv(column.getName()) + separator);
            writer.newLine();
        }
    }
    
    private void generateTabularCsv(BufferedWriter writer, Sheet sheet) throws IOException {
        List<Column> columns = sheet.getColumns();
        if (columns == null || columns.isEmpty()) {
            return;
        }
        
        if (includeHeaders) {
            String header = columns.stream()
                    .map(col -> {
                        String headerText = col.getLabel() != null ? col.getLabel() : col.getName();
                        if (col.getRequired() != null && col.getRequired()) {
                            headerText += " *";
                        }
                        return escapeCsv(headerText);
                    })
                    .collect(Collectors.joining(separator));
            writer.write(header);
            writer.newLine();
        }
        
        for (int i = 0; i < 10; i++) {
            String emptyRow = columns.stream()
                    .map(col -> "")
                    .collect(Collectors.joining(separator));
            writer.write(emptyRow);
            writer.newLine();
        }
    }
    
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        
        if (value.contains("\"") || value.contains(separator) || 
            value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
    
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
    
    public String getSeparator() {
        return separator;
    }
    
    public boolean isIncludeHeaders() {
        return includeHeaders;
    }
}
