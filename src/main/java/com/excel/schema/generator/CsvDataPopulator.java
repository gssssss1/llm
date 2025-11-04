package com.excel.schema.generator;

import com.excel.schema.model.Column;
import com.excel.schema.model.DataType;
import com.excel.schema.model.ExcelSchema;
import com.excel.schema.model.FormatType;
import com.excel.schema.model.Sheet;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CsvDataPopulator {
    
    private final ExcelSchema schema;
    private final String separator;
    private final Map<String, List<List<String>>> sheetData;
    private final DateTimeFormatter dateFormatter;
    
    public CsvDataPopulator(ExcelSchema schema) {
        this(schema, ",");
    }
    
    public CsvDataPopulator(ExcelSchema schema, String separator) {
        this.schema = schema;
        this.separator = separator;
        this.sheetData = new HashMap<>();
        this.dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }
    
    public void populateSheet(String sheetName, Map<String, Object> keyValueData) {
        Sheet sheetDef = findSheetDefinition(sheetName);
        if (sheetDef == null) {
            throw new IllegalArgumentException("Sheet definition not found: " + sheetName);
        }
        
        if (sheetDef.getFormatType() != FormatType.KEY_VALUE) {
            throw new IllegalArgumentException("Sheet " + sheetName + " is not a key-value format");
        }
        
        List<List<String>> rows = new ArrayList<>();
        
        rows.add(Arrays.asList("Key", "Value"));
        
        List<Column> columns = sheetDef.getColumns();
        if (columns != null) {
            for (Column column : columns) {
                String key = column.getName();
                Object value = keyValueData.get(key);
                String valueStr = formatValue(value, column.getDataType());
                rows.add(Arrays.asList(key, valueStr));
            }
        }
        
        sheetData.put(sheetName, rows);
    }
    
    public void populateSheet(String sheetName, List<Map<String, Object>> tabularData) {
        Sheet sheetDef = findSheetDefinition(sheetName);
        if (sheetDef == null) {
            throw new IllegalArgumentException("Sheet definition not found: " + sheetName);
        }
        
        if (sheetDef.getFormatType() != FormatType.TABULAR) {
            throw new IllegalArgumentException("Sheet " + sheetName + " is not a tabular format");
        }
        
        List<List<String>> rows = new ArrayList<>();
        List<Column> columns = sheetDef.getColumns();
        
        if (columns == null || columns.isEmpty()) {
            return;
        }
        
        List<String> headers = columns.stream()
                .map(col -> {
                    String header = col.getLabel() != null ? col.getLabel() : col.getName();
                    if (col.getRequired() != null && col.getRequired()) {
                        header += " *";
                    }
                    return header;
                })
                .collect(Collectors.toList());
        rows.add(headers);
        
        if (tabularData != null) {
            for (Map<String, Object> rowData : tabularData) {
                List<String> row = new ArrayList<>();
                for (Column column : columns) {
                    Object value = rowData.get(column.getName());
                    String valueStr = formatValue(value, column.getDataType());
                    row.add(valueStr);
                }
                rows.add(row);
            }
        }
        
        sheetData.put(sheetName, rows);
    }
    
    public void saveToDirectory(String directoryPath) throws IOException {
        Path dir = Paths.get(directoryPath);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        
        for (Map.Entry<String, List<List<String>>> entry : sheetData.entrySet()) {
            String sheetName = entry.getKey();
            List<List<String>> rows = entry.getValue();
            
            String fileName = sanitizeFileName(sheetName) + ".csv";
            Path filePath = dir.resolve(fileName);
            
            writeCsvFile(filePath.toString(), rows);
        }
    }
    
    public void saveSingleSheet(String sheetName, String filePath) throws IOException {
        List<List<String>> rows = sheetData.get(sheetName);
        if (rows == null) {
            throw new IllegalArgumentException("No data found for sheet: " + sheetName);
        }
        
        writeCsvFile(filePath, rows);
    }
    
    private void writeCsvFile(String filePath, List<List<String>> rows) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            
            for (List<String> row : rows) {
                String line = row.stream()
                        .map(this::escapeCsv)
                        .collect(Collectors.joining(separator));
                writer.write(line);
                writer.newLine();
            }
        }
    }
    
    private String formatValue(Object value, DataType dataType) {
        if (value == null) {
            return "";
        }
        
        if (dataType == null) {
            return value.toString();
        }
        
        switch (dataType) {
            case DATE:
                if (value instanceof LocalDate) {
                    return ((LocalDate) value).format(dateFormatter);
                } else if (value instanceof Date) {
                    return new java.text.SimpleDateFormat("yyyy-MM-dd").format((Date) value);
                }
                return value.toString();
            case BOOLEAN:
                return String.valueOf(value);
            case INTEGER:
            case DOUBLE:
            case STRING:
            case ENUM:
            default:
                return value.toString();
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
    
    private Sheet findSheetDefinition(String sheetName) {
        if (schema.getSchema() == null || schema.getSchema().getSheets() == null) {
            return null;
        }
        
        return schema.getSchema().getSheets().stream()
                .filter(s -> s.getName().equals(sheetName))
                .findFirst()
                .orElse(null);
    }
    
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}
