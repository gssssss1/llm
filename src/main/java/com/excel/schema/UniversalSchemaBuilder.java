package com.excel.schema;

import com.excel.schema.generator.*;
import com.excel.schema.model.ExcelSchema;
import com.excel.schema.parser.SchemaParser;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class UniversalSchemaBuilder {
    
    private ExcelSchema schema;
    private Object generatorResult;
    private Object dataPopulator;
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
    
    public UniversalSchemaBuilder build() {
        switch (fileType) {
            case EXCEL:
                ExcelGenerator excelGen = new ExcelGenerator(schema);
                Workbook workbook = excelGen.generate();
                this.generatorResult = workbook;
                this.dataPopulator = new ExcelDataPopulator(schema, workbook);
                break;
            case CSV:
                this.generatorResult = new CsvGenerator(schema);
                this.dataPopulator = new CsvDataPopulator(schema);
                break;
            default:
                throw new IllegalStateException("Unsupported file type: " + fileType);
        }
        return this;
    }
    
    public UniversalSchemaBuilder addKeyValueData(String sheetName, Map<String, Object> data) {
        if (dataPopulator == null) {
            throw new IllegalStateException("Must call build() before adding data");
        }
        
        if (dataPopulator instanceof ExcelDataPopulator) {
            ((ExcelDataPopulator) dataPopulator).populateSheet(sheetName, data);
        } else if (dataPopulator instanceof CsvDataPopulator) {
            ((CsvDataPopulator) dataPopulator).populateSheet(sheetName, data);
        }
        return this;
    }
    
    public UniversalSchemaBuilder addTabularData(String sheetName, List<Map<String, Object>> data) {
        if (dataPopulator == null) {
            throw new IllegalStateException("Must call build() before adding data");
        }
        
        if (dataPopulator instanceof ExcelDataPopulator) {
            ((ExcelDataPopulator) dataPopulator).populateSheet(sheetName, data);
        } else if (dataPopulator instanceof CsvDataPopulator) {
            ((CsvDataPopulator) dataPopulator).populateSheet(sheetName, data);
        }
        return this;
    }
    
    public void saveTo(String path) throws IOException {
        if (dataPopulator == null) {
            throw new IllegalStateException("Must call build() before saving");
        }
        
        if (dataPopulator instanceof ExcelDataPopulator) {
            ((ExcelDataPopulator) dataPopulator).saveToFile(path);
        } else if (dataPopulator instanceof CsvDataPopulator) {
            ((CsvDataPopulator) dataPopulator).saveToDirectory(path);
        }
    }
    
    public void saveToDirectory(String directoryPath) throws IOException {
        if (fileType != ReportGeneratorFactory.FileType.CSV) {
            throw new IllegalStateException("saveToDirectory() is only supported for CSV format");
        }
        
        if (dataPopulator == null) {
            throw new IllegalStateException("Must call build() before saving");
        }
        
        ((CsvDataPopulator) dataPopulator).saveToDirectory(directoryPath);
    }
    
    public void saveToFile(String filePath) throws IOException {
        if (fileType != ReportGeneratorFactory.FileType.EXCEL) {
            throw new IllegalStateException("saveToFile() is only supported for Excel format");
        }
        
        if (dataPopulator == null) {
            throw new IllegalStateException("Must call build() before saving");
        }
        
        ((ExcelDataPopulator) dataPopulator).saveToFile(filePath);
    }
    
    public Object getGeneratorResult() {
        return generatorResult;
    }
    
    public ExcelSchema getSchema() {
        return schema;
    }
    
    public ReportGeneratorFactory.FileType getFileType() {
        return fileType;
    }
    
    public void close() throws IOException {
        if (generatorResult instanceof Workbook) {
            ((Workbook) generatorResult).close();
        }
    }
}
