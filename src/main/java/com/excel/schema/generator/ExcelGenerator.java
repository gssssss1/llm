package com.excel.schema.generator;

import com.excel.schema.model.Column;
import com.excel.schema.model.ExcelSchema;
import com.excel.schema.model.FormatType;
import com.excel.schema.model.Sheet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class ExcelGenerator {
    
    private final ExcelSchema schema;
    
    public ExcelGenerator(ExcelSchema schema) {
        this.schema = schema;
    }
    
    public Workbook generate() {
        Workbook workbook = new XSSFWorkbook();
        
        if (schema.getSchema() == null || schema.getSchema().getSheets() == null) {
            return workbook;
        }
        
        for (Sheet sheetDef : schema.getSchema().getSheets()) {
            createSheet(workbook, sheetDef);
        }
        
        return workbook;
    }
    
    public void generateToFile(String filePath) throws IOException {
        try (Workbook workbook = generate();
             FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }
    }
    
    public void generateToOutputStream(OutputStream outputStream) throws IOException {
        try (Workbook workbook = generate()) {
            workbook.write(outputStream);
        }
    }
    
    private void createSheet(Workbook workbook, Sheet sheetDef) {
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet(sheetDef.getName());
        
        if (sheetDef.getFormatType() == FormatType.KEY_VALUE) {
            createKeyValueSheet(workbook, sheet, sheetDef);
        } else if (sheetDef.getFormatType() == FormatType.TABULAR) {
            createTabularSheet(workbook, sheet, sheetDef);
        }
    }
    
    private void createKeyValueSheet(Workbook workbook, org.apache.poi.ss.usermodel.Sheet sheet, Sheet sheetDef) {
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
            
            if (column.getRequired() != null && column.getRequired()) {
                valueCell.setCellValue("");
            }
            
            if (column.getEnumValues() != null && !column.getEnumValues().isEmpty()) {
                addDataValidation(sheet, column.getEnumValues(), rowNum - 1, 1);
            }
            
            addComment(workbook, sheet, valueCell, column.getDescription());
        }
        
        sheet.setColumnWidth(0, 8000);
        sheet.setColumnWidth(1, 8000);
    }
    
    private void createTabularSheet(Workbook workbook, org.apache.poi.ss.usermodel.Sheet sheet, Sheet sheetDef) {
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
