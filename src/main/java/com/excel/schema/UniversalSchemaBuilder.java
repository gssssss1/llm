package com.excel.schema;

import com.excel.schema.generator.*;
import com.excel.schema.model.ExcelSchema;
import com.excel.schema.parser.SchemaParser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class UniversalSchemaBuilder {
    
    private ExcelSchema schema;
    private Object generator;
    private ReportGeneratorFactory.FileType fileType;
    
    private UniversalSchemaBuilder(ExcelSchema schema) {
        this.schema = schema;
        this.fileType = ReportGeneratorFactory.FileType.fromString(schema.getReportFileType());
    }
    
    public static UniversalSchemaBuilder fromJson(String json) throws IOException {
        SchemaParser parser = new SchemaParser();
        ExcelSchema schema = parser.parseFromJson(json);
        return new UniversalSchemaBuilder(schema);
    }
    
    public static UniversalSchemaBuilder fromFile(String filePath) throws IOException {
        SchemaParser parser = new SchemaParser();
        ExcelSchema schema = parser.parseFromFile(filePath);
        return new UniversalSchemaBuilder(schema);
    }
    
    public static UniversalSchemaBuilder fromFile(File file) throws IOException {
        SchemaParser parser = new SchemaParser();
        ExcelSchema schema = parser.parseFromFile(file);
        return new UniversalSchemaBuilder(schema);
    }
    
    public static UniversalSchemaBuilder fromSchema(ExcelSchema schema) {
        return new UniversalSchemaBuilder(schema);
    }
    
    public UniversalSchemaBuilder build() throws IOException {
        switch (fileType) {
            case EXCEL:
                ExcelGenerator excelGen = new ExcelGenerator(schema);
                excelGen.generate();
                this.generator = excelGen;
                break;
            case CSV:
                CsvGenerator csvGen = new CsvGenerator(schema);
                this.generator = csvGen;
                break;
            default:
                throw new IllegalStateException("Unsupported file type: " + fileType);
        }
        return this;
    }
    
    public UniversalSchemaBuilder addKeyValueData(String sheetName, Map<String, Object> data) {
        if (generator == null) {
            throw new IllegalStateException("Must call build() before adding data");
        }
        
        if (generator instanceof ExcelGenerator) {
            ((ExcelGenerator) generator).populateSheet(sheetName, data);
        } else if (generator instanceof CsvGenerator) {
            ((CsvGenerator) generator).populateSheet(sheetName, data);
        }
        return this;
    }
    
    public UniversalSchemaBuilder addTabularData(String sheetName, List<Map<String, Object>> data) {
        if (generator == null) {
            throw new IllegalStateException("Must call build() before adding data");
        }
        
        if (generator instanceof ExcelGenerator) {
            ((ExcelGenerator) generator).populateSheet(sheetName, data);
        } else if (generator instanceof CsvGenerator) {
            ((CsvGenerator) generator).populateSheet(sheetName, data);
        }
        return this;
    }
    
    public void saveTo(String path) throws IOException {
        if (generator == null) {
            throw new IllegalStateException("Must call build() before saving");
        }
        
        if (generator instanceof ExcelGenerator) {
            ((ExcelGenerator) generator).saveToFile(path);
        } else if (generator instanceof CsvGenerator) {
            ((CsvGenerator) generator).saveToDirectory(path);
        }
    }
    
    public void saveToDirectory(String directoryPath) throws IOException {
        if (fileType != ReportGeneratorFactory.FileType.CSV) {
            throw new IllegalStateException("saveToDirectory() is only supported for CSV format");
        }
        
        if (generator == null) {
            throw new IllegalStateException("Must call build() before saving");
        }
        
        ((CsvGenerator) generator).saveToDirectory(directoryPath);
    }
    
    public void saveToFile(String filePath) throws IOException {
        if (fileType != ReportGeneratorFactory.FileType.EXCEL) {
            throw new IllegalStateException("saveToFile() is only supported for Excel format");
        }
        
        if (generator == null) {
            throw new IllegalStateException("Must call build() before saving");
        }
        
        ((ExcelGenerator) generator).saveToFile(filePath);
    }
    
    public Object getGenerator() {
        return generator;
    }
    
    public ExcelSchema getSchema() {
        return schema;
    }
    
    public ReportGeneratorFactory.FileType getFileType() {
        return fileType;
    }
    
    public void close() throws IOException {
        if (generator instanceof ExcelGenerator) {
            ExcelGenerator excelGen = (ExcelGenerator) generator;
            if (excelGen.getWorkbook() != null) {
                excelGen.getWorkbook().close();
            }
        }
    }
}
