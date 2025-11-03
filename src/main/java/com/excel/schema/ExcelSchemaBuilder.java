package com.excel.schema;

import com.excel.schema.generator.ExcelDataPopulator;
import com.excel.schema.generator.ExcelGenerator;
import com.excel.schema.model.ExcelSchema;
import com.excel.schema.parser.SchemaParser;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExcelSchemaBuilder {
    
    private ExcelSchema schema;
    private Workbook workbook;
    private ExcelDataPopulator populator;
    
    private ExcelSchemaBuilder(ExcelSchema schema) {
        this.schema = schema;
    }
    
    public static ExcelSchemaBuilder fromJson(String json) throws IOException {
        SchemaParser parser = new SchemaParser();
        ExcelSchema schema = parser.parseFromJson(json);
        return new ExcelSchemaBuilder(schema);
    }
    
    public static ExcelSchemaBuilder fromFile(String filePath) throws IOException {
        SchemaParser parser = new SchemaParser();
        ExcelSchema schema = parser.parseFromFile(filePath);
        return new ExcelSchemaBuilder(schema);
    }
    
    public static ExcelSchemaBuilder fromFile(File file) throws IOException {
        SchemaParser parser = new SchemaParser();
        ExcelSchema schema = parser.parseFromFile(file);
        return new ExcelSchemaBuilder(schema);
    }
    
    public static ExcelSchemaBuilder fromSchema(ExcelSchema schema) {
        return new ExcelSchemaBuilder(schema);
    }
    
    public ExcelSchemaBuilder build() {
        ExcelGenerator generator = new ExcelGenerator(schema);
        this.workbook = generator.generate();
        this.populator = new ExcelDataPopulator(schema, workbook);
        return this;
    }
    
    public ExcelSchemaBuilder addKeyValueData(String sheetName, Map<String, Object> data) {
        if (populator == null) {
            throw new IllegalStateException("Must call build() before adding data");
        }
        populator.populateSheet(sheetName, data);
        return this;
    }
    
    public ExcelSchemaBuilder addTabularData(String sheetName, List<Map<String, Object>> data) {
        if (populator == null) {
            throw new IllegalStateException("Must call build() before adding data");
        }
        populator.populateSheet(sheetName, data);
        return this;
    }
    
    public void saveTo(String filePath) throws IOException {
        if (populator == null) {
            throw new IllegalStateException("Must call build() before saving");
        }
        populator.saveToFile(filePath);
    }
    
    public Workbook getWorkbook() {
        return workbook;
    }
    
    public ExcelSchema getSchema() {
        return schema;
    }
    
    public void close() throws IOException {
        if (workbook != null) {
            workbook.close();
        }
    }
}
