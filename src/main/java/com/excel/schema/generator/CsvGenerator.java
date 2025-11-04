package com.excel.schema.generator;

import com.excel.schema.model.Column;
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
import java.util.ArrayList;
import java.util.List;

public class CsvGenerator {
    
    private final ExcelSchema schema;
    private final CSVFormat csvFormat;
    private final boolean includeHeaders;
    
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
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8));
             CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
            
            if (sheet.getFormatType() == FormatType.KEY_VALUE) {
                generateKeyValueCsv(csvPrinter, sheet);
            } else if (sheet.getFormatType() == FormatType.TABULAR) {
                generateTabularCsv(csvPrinter, sheet);
            }
        }
    }
    
    private void generateKeyValueCsv(CSVPrinter csvPrinter, Sheet sheet) throws IOException {
        List<Column> columns = sheet.getColumns();
        if (columns == null || columns.isEmpty()) {
            return;
        }
        
        if (includeHeaders) {
            csvPrinter.printRecord("Key", "Value");
        }
        
        for (Column column : columns) {
            csvPrinter.printRecord(column.getName(), "");
        }
    }
    
    private void generateTabularCsv(CSVPrinter csvPrinter, Sheet sheet) throws IOException {
        List<Column> columns = sheet.getColumns();
        if (columns == null || columns.isEmpty()) {
            return;
        }
        
        if (includeHeaders) {
            List<String> headers = new ArrayList<>();
            for (Column col : columns) {
                String headerText = col.getLabel() != null ? col.getLabel() : col.getName();
                if (col.getRequired() != null && col.getRequired()) {
                    headerText += " *";
                }
                headers.add(headerText);
            }
            csvPrinter.printRecord(headers);
        }
        
        for (int i = 0; i < 10; i++) {
            List<String> emptyRow = new ArrayList<>();
            for (int j = 0; j < columns.size(); j++) {
                emptyRow.add("");
            }
            csvPrinter.printRecord(emptyRow);
        }
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
