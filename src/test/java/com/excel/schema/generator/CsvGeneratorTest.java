package com.excel.schema.generator;

import com.excel.schema.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvGeneratorTest {
    
    @TempDir
    Path tempDir;
    
    @Test
    void testGenerateKeyValueCsv() throws IOException {
        Column column = Column.builder()
                .name("Study Name")
                .dataType(DataType.STRING)
                .description("Study name field")
                .required(true)
                .build();
        
        Sheet sheet = Sheet.builder()
                .name("Index")
                .formatType(FormatType.KEY_VALUE)
                .description("Index sheet")
                .columns(Collections.singletonList(column))
                .build();
        
        ExcelSchema.Schema schemaObj = ExcelSchema.Schema.builder()
                .sheets(Collections.singletonList(sheet))
                .build();
        
        ExcelSchema schema = ExcelSchema.builder()
                .reportName("Test Report")
                .reportFileType("csv")
                .schema(schemaObj)
                .build();
        
        CsvGenerator generator = new CsvGenerator(schema);
        
        String filePath = tempDir.resolve("test.csv").toString();
        generator.generateSheet(sheet, filePath);
        
        assertTrue(Files.exists(Path.of(filePath)));
        
        List<String> lines = Files.readAllLines(Path.of(filePath));
        assertEquals(2, lines.size());
        assertEquals("Key,Value", lines.get(0));
        assertEquals("Study Name,", lines.get(1));
    }
    
    @Test
    void testGenerateTabularCsv() throws IOException {
        Column col1 = Column.builder()
                .name("ID")
                .dataType(DataType.STRING)
                .required(true)
                .build();
        
        Column col2 = Column.builder()
                .name("Name")
                .dataType(DataType.STRING)
                .label("Full Name")
                .required(false)
                .build();
        
        Sheet sheet = Sheet.builder()
                .name("Data")
                .formatType(FormatType.TABULAR)
                .columns(List.of(col1, col2))
                .build();
        
        ExcelSchema.Schema schemaObj = ExcelSchema.Schema.builder()
                .sheets(Collections.singletonList(sheet))
                .build();
        
        ExcelSchema schema = ExcelSchema.builder()
                .reportName("Test Report")
                .reportFileType("csv")
                .schema(schemaObj)
                .build();
        
        CsvGenerator generator = new CsvGenerator(schema);
        
        String filePath = tempDir.resolve("test_tabular.csv").toString();
        generator.generateSheet(sheet, filePath);
        
        assertTrue(Files.exists(Path.of(filePath)));
        
        List<String> lines = Files.readAllLines(Path.of(filePath));
        assertTrue(lines.size() > 1);
        assertEquals("ID *,Full Name", lines.get(0));
    }
    
    @Test
    void testGenerateToDirectory() throws IOException {
        Column column = Column.builder()
                .name("Field1")
                .dataType(DataType.STRING)
                .build();
        
        Sheet sheet1 = Sheet.builder()
                .name("Sheet1")
                .formatType(FormatType.KEY_VALUE)
                .columns(Collections.singletonList(column))
                .build();
        
        Sheet sheet2 = Sheet.builder()
                .name("Sheet2")
                .formatType(FormatType.TABULAR)
                .columns(Collections.singletonList(column))
                .build();
        
        ExcelSchema.Schema schemaObj = ExcelSchema.Schema.builder()
                .sheets(List.of(sheet1, sheet2))
                .build();
        
        ExcelSchema schema = ExcelSchema.builder()
                .reportName("Multi-Sheet Report")
                .reportFileType("csv")
                .schema(schemaObj)
                .build();
        
        CsvGenerator generator = new CsvGenerator(schema);
        
        String outputDir = tempDir.resolve("csv_output").toString();
        generator.generateToDirectory(outputDir);
        
        assertTrue(Files.exists(Path.of(outputDir, "Sheet1.csv")));
        assertTrue(Files.exists(Path.of(outputDir, "Sheet2.csv")));
    }
    
    @Test
    void testCsvEscaping() throws IOException {
        Column column = Column.builder()
                .name("Text with, comma")
                .dataType(DataType.STRING)
                .build();
        
        Sheet sheet = Sheet.builder()
                .name("Test")
                .formatType(FormatType.KEY_VALUE)
                .columns(Collections.singletonList(column))
                .build();
        
        ExcelSchema.Schema schemaObj = ExcelSchema.Schema.builder()
                .sheets(Collections.singletonList(sheet))
                .build();
        
        ExcelSchema schema = ExcelSchema.builder()
                .reportName("Test")
                .reportFileType("csv")
                .schema(schemaObj)
                .build();
        
        CsvGenerator generator = new CsvGenerator(schema);
        
        String filePath = tempDir.resolve("escape_test.csv").toString();
        generator.generateSheet(sheet, filePath);
        
        List<String> lines = Files.readAllLines(Path.of(filePath));
        assertTrue(lines.get(1).contains("\"Text with, comma\""));
    }
}
