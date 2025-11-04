package com.excel.schema.generator;

import com.excel.schema.model.*;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ExcelGeneratorTest {
    
    @Test
    void testGenerateEmptyWorkbook() {
        ExcelSchema schema = ExcelSchema.builder()
                .reportName("Test")
                .reportFileType("excel")
                .build();
        
        ExcelGenerator generator = new ExcelGenerator(schema);
        Workbook workbook = generator.generate();
        
        assertNotNull(workbook);
        assertEquals(0, workbook.getNumberOfSheets());
        
        try {
            workbook.close();
        } catch (Exception e) {
            fail("Failed to close workbook: " + e.getMessage());
        }
    }
    
    @Test
    void testGenerateKeyValueSheet() {
        Column column = Column.builder()
                .name("Study Name")
                .dataType(DataType.STRING)
                .description("Study name field")
                .required(true)
                .build();
        
        com.excel.schema.model.Sheet sheetDef = com.excel.schema.model.Sheet.builder()
                .name("Index")
                .formatType(FormatType.KEY_VALUE)
                .description("Index sheet")
                .columns(Collections.singletonList(column))
                .build();
        
        ExcelSchema.Schema schemaObj = ExcelSchema.Schema.builder()
                .sheets(Collections.singletonList(sheetDef))
                .build();
        
        ExcelSchema schema = ExcelSchema.builder()
                .reportName("Test Report")
                .reportFileType("excel")
                .schema(schemaObj)
                .build();
        
        ExcelGenerator generator = new ExcelGenerator(schema);
        Workbook workbook = generator.generate();
        
        assertNotNull(workbook);
        assertEquals(1, workbook.getNumberOfSheets());
        
        Sheet sheet = workbook.getSheet("Index");
        assertNotNull(sheet);
        assertEquals(1, sheet.getPhysicalNumberOfRows());
        
        var row = sheet.getRow(0);
        assertNotNull(row);
        assertEquals("Study Name", row.getCell(0).getStringCellValue());
        
        try {
            workbook.close();
        } catch (Exception e) {
            fail("Failed to close workbook: " + e.getMessage());
        }
    }
    
    @Test
    void testGenerateTabularSheet() {
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
        
        com.excel.schema.model.Sheet sheetDef = com.excel.schema.model.Sheet.builder()
                .name("Data")
                .formatType(FormatType.TABULAR)
                .columns(Arrays.asList(col1, col2))
                .build();
        
        ExcelSchema.Schema schemaObj = ExcelSchema.Schema.builder()
                .sheets(Collections.singletonList(sheetDef))
                .build();
        
        ExcelSchema schema = ExcelSchema.builder()
                .reportName("Test Report")
                .reportFileType("excel")
                .schema(schemaObj)
                .build();
        
        ExcelGenerator generator = new ExcelGenerator(schema);
        Workbook workbook = generator.generate();
        
        assertNotNull(workbook);
        Sheet sheet = workbook.getSheet("Data");
        assertNotNull(sheet);
        
        var headerRow = sheet.getRow(0);
        assertNotNull(headerRow);
        assertEquals("ID *", headerRow.getCell(0).getStringCellValue());
        assertEquals("Full Name", headerRow.getCell(1).getStringCellValue());
        
        assertTrue(sheet.getPhysicalNumberOfRows() > 1);
        
        try {
            workbook.close();
        } catch (Exception e) {
            fail("Failed to close workbook: " + e.getMessage());
        }
    }
    
    @Test
    void testGenerateMultipleSheets() {
        com.excel.schema.model.Sheet sheet1 = com.excel.schema.model.Sheet.builder()
                .name("Sheet1")
                .formatType(FormatType.KEY_VALUE)
                .columns(Collections.singletonList(
                        Column.builder().name("Field1").dataType(DataType.STRING).build()
                ))
                .build();
        
        com.excel.schema.model.Sheet sheet2 = com.excel.schema.model.Sheet.builder()
                .name("Sheet2")
                .formatType(FormatType.TABULAR)
                .columns(Collections.singletonList(
                        Column.builder().name("Column1").dataType(DataType.STRING).build()
                ))
                .build();
        
        ExcelSchema.Schema schemaObj = ExcelSchema.Schema.builder()
                .sheets(Arrays.asList(sheet1, sheet2))
                .build();
        
        ExcelSchema schema = ExcelSchema.builder()
                .reportName("Multi-Sheet Report")
                .reportFileType("excel")
                .schema(schemaObj)
                .build();
        
        ExcelGenerator generator = new ExcelGenerator(schema);
        Workbook workbook = generator.generate();
        
        assertEquals(2, workbook.getNumberOfSheets());
        assertNotNull(workbook.getSheet("Sheet1"));
        assertNotNull(workbook.getSheet("Sheet2"));
        
        try {
            workbook.close();
        } catch (Exception e) {
            fail("Failed to close workbook: " + e.getMessage());
        }
    }
}
