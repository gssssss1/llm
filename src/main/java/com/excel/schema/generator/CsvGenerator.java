package com.excel.schema.generator;

import com.excel.schema.model.Column;
import com.excel.schema.model.DataType;
import com.excel.schema.model.ExcelSchema;
import com.excel.schema.model.FormatType;
import com.excel.schema.model.Sheet;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CsvGenerator {
    
    private final ExcelSchema schema;
    private final CSVFormat csvFormat;
    private final boolean includeHeaders;
    private final Map<String, List<List<String>>> sheetData;
    private final DateTimeFormatter dateFormatter;
    private String outputDirectory;
    
    public CsvGenerator(ExcelSchema schema) {
        this(schema, CSVFormat.DEFAULT, true);
    }
    
    public CsvGenerator(ExcelSchema schema, String separator, boolean includeHeaders) {
        this(schema, CSVFormat.DEFAULT.withDelimiter(separator.charAt(0)), includeHeaders);
    }
    
    public CsvGenerator(ExcelSchema schema, CSVFormat csvFormat, boolean includeHeaders) {
        this.schema = schema;
        this.csvFormat = csvFormat;
        this.includeHeaders = includeHeaders;
        this.sheetData = new HashMap<>();
        this.dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }
    
    public void generate(String directoryPath) throws IOException {
        this.outputDirectory = directoryPath;
        Path dir = Paths.get(directoryPath);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        
        if (schema.getSchema() == null || schema.getSchema().getSheets() == null) {
            return;
        }
        
        for (Sheet sheet : schema.getSchema().getSheets()) {
            generateEmptySheet(sheet);
        }
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
        
        if (includeHeaders) {
            rows.add(Arrays.asList("Key", "Value"));
        }
        
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
        
        if (includeHeaders) {
            List<String> headers = new ArrayList<>();
            for (Column col : columns) {
                String header = col.getLabel() != null ? col.getLabel() : col.getName();
                if (col.getRequired() != null && col.getRequired()) {
                    header += " *";
                }
                headers.add(header);
            }
            rows.add(headers);
        }
        
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
    
    public void saveToDirectory() throws IOException {
        if (outputDirectory == null) {
            throw new IllegalStateException("Must call generate() before saving");
        }
        saveToDirectory(outputDirectory);
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
    
    private void generateEmptySheet(Sheet sheet) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        
        if (sheet.getFormatType() == FormatType.KEY_VALUE) {
            if (includeHeaders) {
                rows.add(Arrays.asList("Key", "Value"));
            }
            
            List<Column> columns = sheet.getColumns();
            if (columns != null) {
                for (Column column : columns) {
                    rows.add(Arrays.asList(column.getName(), ""));
                }
            }
        } else if (sheet.getFormatType() == FormatType.TABULAR) {
            List<Column> columns = sheet.getColumns();
            if (columns != null && !columns.isEmpty()) {
                if (includeHeaders) {
                    List<String> headers = new ArrayList<>();
                    for (Column col : columns) {
                        String headerText = col.getLabel() != null ? col.getLabel() : col.getName();
                        if (col.getRequired() != null && col.getRequired()) {
                            headerText += " *";
                        }
                        headers.add(headerText);
                    }
                    rows.add(headers);
                }
                
                for (int i = 0; i < 10; i++) {
                    List<String> emptyRow = new ArrayList<>();
                    for (int j = 0; j < columns.size(); j++) {
                        emptyRow.add("");
                    }
                    rows.add(emptyRow);
                }
            }
        }
        
        sheetData.put(sheet.getName(), rows);
    }
    
    private void writeCsvFile(String filePath, List<List<String>> rows) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8));
             CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
            
            for (List<String> row : rows) {
                csvPrinter.printRecord(row);
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
    
    public CSVFormat getCsvFormat() {
        return csvFormat;
    }
    
    public boolean isIncludeHeaders() {
        return includeHeaders;
    }
}
