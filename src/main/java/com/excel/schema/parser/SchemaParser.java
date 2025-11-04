package com.excel.schema.parser;

import com.excel.schema.model.ExcelSchema;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SchemaParser {
    
    private final ObjectMapper objectMapper;
    
    public SchemaParser() {
        this.objectMapper = new ObjectMapper();
    }
    
    public SchemaParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    public ExcelSchema parseFromJson(String json) throws IOException {
        return objectMapper.readValue(json, ExcelSchema.class);
    }
    
    public ExcelSchema parseFromFile(File file) throws IOException {
        return objectMapper.readValue(file, ExcelSchema.class);
    }
    
    public ExcelSchema parseFromFile(String filePath) throws IOException {
        return parseFromFile(new File(filePath));
    }
    
    public ExcelSchema parseFromInputStream(InputStream inputStream) throws IOException {
        return objectMapper.readValue(inputStream, ExcelSchema.class);
    }
}
