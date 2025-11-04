package com.excel.schema.generator;

import com.excel.schema.model.Column;
import com.excel.schema.model.DataType;
import com.excel.schema.model.ExcelSchema;
import com.excel.schema.model.FormatType;
import com.excel.schema.model.Sheet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelGenerator {
    
    private final ExcelSchema schema;
    private Workbook workbook;
    private final Map<String, org.apache.poi.ss.usermodel.Sheet> sheetMap;
    
    public ExcelGenerator(ExcelSchema schema) {
        this.schema = schema;
        this.sheetMap = new HashMap<>();
    }
    
    public Workbook generate() {
        workbook = new XSSFWorkbook();
        
        if (schema.getSchema() == null || schema.getSchema().getSheets() == null) {
            return workbook;
        }
        
        for (Sheet sheetDef : schema.getSchema().getSheets()) {
            createSheet(sheetDef);
        }
        
        return workbook;
    }
    
    public void populateSheet(String sheetName, Map<String, Object> keyValueData) {
        Sheet sheetDef = findSheetDefinition(sheetName);
        if (sheetDef == null) {
            throw new IllegalArgumentException("Sheet definition not found: " + sheetName);
        }
        
        if (sheetDef.getFormatType() != FormatType.KEY_VALUE) {
            throw new IllegalArgumentException("Sheet " + sheetName + " is not a key-value format");
        }
        
        org.apache.poi.ss.usermodel.Sheet sheet = sheetMap.get(sheetName);
        if (sheet == null) {
            throw new IllegalArgumentException("Sheet not found: " + sheetName);
        }
        
        populateKeyValueData(sheet, sheetDef, keyValueData);
    }
    
    public void populateSheet(String sheetName, List<Map<String, Object>> tabularData) {
        Sheet sheetDef = findSheetDefinition(sheetName);
        if (sheetDef == null) {
            throw new IllegalArgumentException("Sheet definition not found: " + sheetName);
        }
        
        if (sheetDef.getFormatType() != FormatType.TABULAR) {
            throw new IllegalArgumentException("Sheet " + sheetName + " is not a tabular format");
        }
        
        org.apache.poi.ss.usermodel.Sheet sheet = sheetMap.get(sheetName);
        if (sheet == null) {
            throw new IllegalArgumentException("Sheet not found: " + sheetName);
        }
        
        populateTabularData(sheet, sheetDef, tabularData);
    }
    
    public void saveToFile(String filePath) throws IOException {
        if (workbook == null) {
            throw new IllegalStateException("Must call generate() before saving");
        }
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }
    }
    
    public void saveToOutputStream(OutputStream outputStream) throws IOException {
        if (workbook == null) {
            throw new IllegalStateException("Must call generate() before saving");
        }
        workbook.write(outputStream);
    }
    
    public Workbook getWorkbook() {
        return workbook;
    }
    
    private void createSheet(Sheet sheetDef) {
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet(sheetDef.getName());
        sheetMap.put(sheetDef.getName(), sheet);
        
        if (sheetDef.getFormatType() == FormatType.KEY_VALUE) {
            createKeyValueSheet(sheet, sheetDef);
        } else if (sheetDef.getFormatType() == FormatType.TABULAR) {
            createTabularSheet(sheet, sheetDef);
        }
    }
    
    private void createKeyValueSheet(org.apache.poi.ss.usermodel.Sheet sheet, Sheet sheetDef) {
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle valueStyle = createValueStyle(workbook);
        
        List<Column> columns = sheetDef.getColumns();
        if (columns == null || columns.isEmpty()) {
            return;
        }
        
        int rowNum = 0;
        for (Column column : columns) {
            Row row = sheet.createRow(rowNum++);
            
            Cell keyCell = row.createCell(0);
            keyCell.setCellValue(column.getName());
            keyCell.setCellStyle(headerStyle);
            
            Cell valueCell = row.createCell(1);
            valueCell.setCellStyle(valueStyle);
            
            if (column.getEnumValues() != null && !column.getEnumValues().isEmpty()) {
                addDataValidation(sheet, column.getEnumValues(), rowNum - 1, 1);
            }
            
            addComment(workbook, sheet, valueCell, column.getDescription());
        }
        
        sheet.setColumnWidth(0, 8000);
        sheet.setColumnWidth(1, 8000);
    }
    
    private void createTabularSheet(org.apache.poi.ss.usermodel.Sheet sheet, Sheet sheetDef) {
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle valueStyle = createValueStyle(workbook);
        
        List<Column> columns = sheetDef.getColumns();
        if (columns == null || columns.isEmpty()) {
            return;
        }
        
        Row headerRow = sheet.createRow(0);
        
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            Cell headerCell = headerRow.createCell(i);
            
            String headerText = column.getLabel() != null ? column.getLabel() : column.getName();
            if (column.getRequired() != null && column.getRequired()) {
                headerText += " *";
            }
            
            headerCell.setCellValue(headerText);
            headerCell.setCellStyle(headerStyle);
            
            addComment(workbook, sheet, headerCell, column.getDescription());
            
            if (column.getEnumValues() != null && !column.getEnumValues().isEmpty()) {
                addDataValidation(sheet, column.getEnumValues(), 1, i);
            }
            
            sheet.setColumnWidth(i, 6000);
        }
        
        for (int i = 1; i <= 10; i++) {
            Row dataRow = sheet.createRow(i);
            for (int j = 0; j < columns.size(); j++) {
                Cell cell = dataRow.createCell(j);
                cell.setCellStyle(valueStyle);
            }
        }
    }
    
    private void populateKeyValueData(org.apache.poi.ss.usermodel.Sheet sheet, Sheet sheetDef, Map<String, Object> data) {
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
    
    private void populateTabularData(org.apache.poi.ss.usermodel.Sheet sheet, Sheet sheetDef, List<Map<String, Object>> data) {
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
        
        if (dataType == null) {
            cell.setCellValue(value.toString());
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
                CellStyle cellStyle = workbook.createCellStyle();
                CreationHelper createHelper = workbook.getCreationHelper();
                cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));
                cell.setCellStyle(cellStyle);
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
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createValueStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
    
    private void addDataValidation(org.apache.poi.ss.usermodel.Sheet sheet, List<String> values, int startRow, int col) {
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(
            values.toArray(new String[0])
        );
        
        CellRangeAddressList addressList = new CellRangeAddressList(
            startRow, startRow + 1000, col, col
        );
        
        DataValidation validation = validationHelper.createValidation(constraint, addressList);
        validation.setShowErrorBox(true);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.createErrorBox("Invalid Value", "Please select a value from the dropdown list.");
        sheet.addValidationData(validation);
    }
    
    private void addComment(Workbook workbook, org.apache.poi.ss.usermodel.Sheet sheet, Cell cell, String commentText) {
        if (commentText == null || commentText.trim().isEmpty()) {
            return;
        }
        
        CreationHelper factory = workbook.getCreationHelper();
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + 3);
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex() + 3);
        
        Comment comment = drawing.createCellComment(anchor);
        RichTextString str = factory.createRichTextString(commentText);
        comment.setString(str);
        cell.setCellComment(comment);
    }
}
