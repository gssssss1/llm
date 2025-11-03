package com.excel.schema.parser;

import com.excel.schema.model.DataType;
import com.excel.schema.model.ExcelSchema;
import com.excel.schema.model.FormatType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SchemaParserTest {
    
    private SchemaParser parser;
    
    @BeforeEach
    void setUp() {
        parser = new SchemaParser();
    }
    
    @Test
    void testParseFromJson() throws IOException {
        String json = """
            {
                "report_name": "Test Report",
                "report_file_type": "excel",
                "description": "Test Description",
                "schema": {
                    "sheet": [
                        {
                            "name": "Test Sheet",
                            "format_type": "key_value",
                            "description": "Test sheet description",
                            "columns": [
                                {
                                    "name": "Field1",
                                    "data_type": "string",
                                    "description": "Field 1 description",
                                    "required": true
                                }
                            ]
                        }
                    ]
                }
            }
            """;
        
        ExcelSchema schema = parser.parseFromJson(json);
        
        assertNotNull(schema);
        assertEquals("Test Report", schema.getReportName());
        assertEquals("excel", schema.getReportFileType());
        assertEquals("Test Description", schema.getDescription());
        
        assertNotNull(schema.getSchema());
        assertNotNull(schema.getSchema().getSheets());
        assertEquals(1, schema.getSchema().getSheets().size());
        
        var sheet = schema.getSchema().getSheets().get(0);
        assertEquals("Test Sheet", sheet.getName());
        assertEquals(FormatType.KEY_VALUE, sheet.getFormatType());
        assertEquals("Test sheet description", sheet.getDescription());
        
        assertNotNull(sheet.getColumns());
        assertEquals(1, sheet.getColumns().size());
        
        var column = sheet.getColumns().get(0);
        assertEquals("Field1", column.getName());
        assertEquals(DataType.STRING, column.getDataType());
        assertEquals("Field 1 description", column.getDescription());
        assertTrue(column.getRequired());
    }
    
    @Test
    void testParseTabularFormat() throws IOException {
        String json = """
            {
                "report_name": "Tabular Report",
                "report_file_type": "excel",
                "description": "",
                "schema": {
                    "sheet": [
                        {
                            "name": "Data Sheet",
                            "format_type": "tabular",
                            "columns": [
                                {
                                    "name": "ID",
                                    "data_type": "integer",
                                    "required": true
                                },
                                {
                                    "name": "Name",
                                    "data_type": "string",
                                    "required": false
                                }
                            ]
                        }
                    ]
                }
            }
            """;
        
        ExcelSchema schema = parser.parseFromJson(json);
        
        assertNotNull(schema);
        var sheet = schema.getSchema().getSheets().get(0);
        assertEquals(FormatType.TABULAR, sheet.getFormatType());
        assertEquals(2, sheet.getColumns().size());
    }
    
    @Test
    void testParseEnumColumn() throws IOException {
        String json = """
            {
                "report_name": "Enum Report",
                "report_file_type": "excel",
                "description": "",
                "schema": {
                    "sheet": [
                        {
                            "name": "Enum Sheet",
                            "format_type": "tabular",
                            "columns": [
                                {
                                    "name": "Status",
                                    "data_type": "enum",
                                    "label": "Status",
                                    "enum_values": ["Active", "Inactive", "Pending"],
                                    "required": true
                                }
                            ]
                        }
                    ]
                }
            }
            """;
        
        ExcelSchema schema = parser.parseFromJson(json);
        
        var column = schema.getSchema().getSheets().get(0).getColumns().get(0);
        assertEquals(DataType.ENUM, column.getDataType());
        assertNotNull(column.getEnumValues());
        assertEquals(3, column.getEnumValues().size());
        assertTrue(column.getEnumValues().contains("Active"));
        assertTrue(column.getEnumValues().contains("Inactive"));
        assertTrue(column.getEnumValues().contains("Pending"));
    }
}
