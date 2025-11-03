package com.excel.schema.generator;

import com.excel.schema.model.Column;
import com.excel.schema.model.DataType;
import com.excel.schema.model.ExcelSchema;
import com.excel.schema.model.FormatType;
import com.excel.schema.model.Sheet;
import org.apache.poi.ss.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ExcelDataPopulator {
    
    private final ExcelSchema schema;
    private final Workbook workbook;
    
    public ExcelDataPopulator(ExcelSchema schema, Workbook workbook) {
        this.schema = schema;
        this.workbook = workbook;
    }
    
    public void populateSheet(String sheetName, Map<String, Object> keyValueData) {
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            throw new IllegalArgumentException("Sheet not found: " + sheetName);
        }
        
        Sheet sheetDef = findSheetDefinition(sheetName);
        if (sheetDef == null) {
            throw new IllegalArgumentException("Sheet definition not found: " + sheetName);
        }
        
        if (sheetDef.getFormatType() == FormatType.KEY_VALUE) {
            populateKeyValueSheet(sheet, sheetDef, keyValueData);
        }
    }
    
    public void populateSheet(String sheetName, List<Map<String, Object>> tabularData) {
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            throw new IllegalArgumentException("Sheet not found: " + sheetName);
        }
        
        Sheet sheetDef = findSheetDefinition(sheetName);
        if (sheetDef == null) {
            throw new IllegalArgumentException("Sheet definition not found: " + sheetName);
        }
        
        if (sheetDef.getFormatType() == FormatType.TABULAR) {
            populateTabularSheet(sheet, sheetDef, tabularData);
        }
    }
    
    private void populateKeyValueSheet(org.apache.poi.ss.usermodel.Sheet sheet, Sheet sheetDef, Map<String, Object> data) {
        List<Column> columns = sheetDef.getColumns();
        if (columns == null || columns.isEmpty()) {
            return;
        }
        
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            
            Cell valueCell = row.getCell(1);
            if (valueCell == null) {
                valueCell = row.createCell(1);
            }
            
            Object value = data.get(column.getName());
            if (value != null) {
                setCellValue(valueCell, value, column.getDataType());
            }
        }
    }
    
    private void populateTabularSheet(org.apache.poi.ss.usermodel.Sheet sheet, Sheet sheetDef, List<Map<String, Object>> data) {
        List<Column> columns = sheetDef.getColumns();
        if (columns == null || columns.isEmpty() || data == null || data.isEmpty()) {
            return;
        }
        
        for (int rowIndex = 0; rowIndex < data.size(); rowIndex++) {
            Map<String, Object> rowData = data.get(rowIndex);
            Row row = sheet.getRow(rowIndex + 1);
            
            if (row == null) {
                row = sheet.createRow(rowIndex + 1);
            }
            
            for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
                Column column = columns.get(colIndex);
                Cell cell = row.getCell(colIndex);
                
                if (cell == null) {
                    cell = row.createCell(colIndex);
                }
                
                Object value = rowData.get(column.getName());
                if (value != null) {
                    setCellValue(cell, value, column.getDataType());
                }
            }
        }
    }
    
    private void setCellValue(Cell cell, Object value, DataType dataType) {
        if (value == null) {
            cell.setBlank();
            return;
        }
        
        switch (dataType) {
            case STRING:
            case ENUM:
                cell.setCellValue(value.toString());
                break;
            case INTEGER:
                if (value instanceof Number) {
                    cell.setCellValue(((Number) value).intValue());
                } else {
                    cell.setCellValue(Integer.parseInt(value.toString()));
                }
                break;
            case DOUBLE:
                if (value instanceof Number) {
                    cell.setCellValue(((Number) value).doubleValue());
                } else {
                    cell.setCellValue(Double.parseDouble(value.toString()));
                }
                break;
            case BOOLEAN:
                if (value instanceof Boolean) {
                    cell.setCellValue((Boolean) value);
                } else {
                    cell.setCellValue(Boolean.parseBoolean(value.toString()));
                }
                break;
            case DATE:
                if (value instanceof Date) {
                    cell.setCellValue((Date) value);
                } else if (value instanceof LocalDate) {
                    Date date = Date.from(((LocalDate) value).atStartOfDay(ZoneId.systemDefault()).toInstant());
                    cell.setCellValue(date);
                } else {
                    cell.setCellValue(value.toString());
                }
                CellStyle cellStyle = cell.getCellStyle();
                CreationHelper createHelper = workbook.getCreationHelper();
                cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));
                break;
            default:
                cell.setCellValue(value.toString());
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
    
    public void saveToFile(String filePath) throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }
    }
}
