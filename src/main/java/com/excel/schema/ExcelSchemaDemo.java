package com.excel.schema;

import com.excel.schema.generator.ExcelDataPopulator;
import com.excel.schema.generator.ExcelGenerator;
import com.excel.schema.model.ExcelSchema;
import com.excel.schema.parser.SchemaParser;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.util.*;

public class ExcelSchemaDemo {
    
    public static void main(String[] args) {
        try {
            String schemaJson = """
                {
                    "report_name": "Clinical Study Report",
                    "report_file_type": "excel",
                    "description": "Clinical study report with multiple sheets",
                    "schema": {
                        "sheet": [
                            {
                                "name": "Index",
                                "format_type": "key_value",
                                "description": "Index contains metadata of the task",
                                "columns": [
                                    {
                                        "name": "Study Name",
                                        "data_type": "string",
                                        "description": "Study name of the task",
                                        "required": true
                                    },
                                    {
                                        "name": "Study Date",
                                        "data_type": "string",
                                        "description": "Study date",
                                        "required": true
                                    },
                                    {
                                        "name": "Principal Investigator",
                                        "data_type": "string",
                                        "description": "Name of PI",
                                        "required": false
                                    }
                                ]
                            },
                            {
                                "name": "Eligibility Guardrail",
                                "format_type": "tabular",
                                "description": "Business name of the task",
                                "columns": [
                                    {
                                        "name": "ELIG_Measurable_Lesion_V1",
                                        "data_type": "enum",
                                        "label": "ELIG_Measurable_Lesion_V1",
                                        "enum_values": ["a", "b"],
                                        "description": "Version of the business",
                                        "required": true
                                    },
                                    {
                                        "name": "Status",
                                        "data_type": "enum",
                                        "label": "Status",
                                        "enum_values": ["Active", "Inactive", "Pending"],
                                        "description": "Current status",
                                        "required": true
                                    }
                                ]
                            },
                            {
                                "name": "Rules",
                                "format_type": "tabular",
                                "description": "Rules of validation",
                                "columns": [
                                    {
                                        "name": "ID",
                                        "data_type": "string",
                                        "description": "ID of the validation rule",
                                        "required": true
                                    },
                                    {
                                        "name": "Rule Name",
                                        "data_type": "string",
                                        "description": "Name of the rule",
                                        "required": true
                                    },
                                    {
                                        "name": "Severity",
                                        "data_type": "enum",
                                        "enum_values": ["Error", "Warning", "Info"],
                                        "description": "Severity level",
                                        "required": false
                                    }
                                ]
                            },
                            {
                                "name": "ELIG_Age_18_75_V1",
                                "description": "Rule name",
                                "format_type": "tabular",
                                "columns": [
                                    {
                                        "name": "USUBJID",
                                        "data_type": "string",
                                        "description": "Subject ID",
                                        "required": true
                                    },
                                    {
                                        "name": "Age",
                                        "data_type": "integer",
                                        "description": "Subject age",
                                        "required": true
                                    },
                                    {
                                        "name": "Eligible",
                                        "data_type": "boolean",
                                        "description": "Is eligible",
                                        "required": true
                                    }
                                ]
                            }
                        ]
                    }
                }
                """;
            
            SchemaParser parser = new SchemaParser();
            ExcelSchema schema = parser.parseFromJson(schemaJson);
            
            ExcelGenerator generator = new ExcelGenerator(schema);
            Workbook workbook = generator.generate();
            
            ExcelDataPopulator populator = new ExcelDataPopulator(schema, workbook);
            
            Map<String, Object> indexData = new HashMap<>();
            indexData.put("Study Name", "Clinical Trial ABC-123");
            indexData.put("Study Date", "2024-01-15");
            indexData.put("Principal Investigator", "Dr. John Smith");
            populator.populateSheet("Index", indexData);
            
            List<Map<String, Object>> eligibilityData = new ArrayList<>();
            Map<String, Object> row1 = new HashMap<>();
            row1.put("ELIG_Measurable_Lesion_V1", "a");
            row1.put("Status", "Active");
            eligibilityData.add(row1);
            
            Map<String, Object> row2 = new HashMap<>();
            row2.put("ELIG_Measurable_Lesion_V1", "b");
            row2.put("Status", "Inactive");
            eligibilityData.add(row2);
            
            populator.populateSheet("Eligibility Guardrail", eligibilityData);
            
            List<Map<String, Object>> rulesData = new ArrayList<>();
            Map<String, Object> rule1 = new HashMap<>();
            rule1.put("ID", "R001");
            rule1.put("Rule Name", "Age Validation");
            rule1.put("Severity", "Error");
            rulesData.add(rule1);
            
            Map<String, Object> rule2 = new HashMap<>();
            rule2.put("ID", "R002");
            rule2.put("Rule Name", "Date Range Check");
            rule2.put("Severity", "Warning");
            rulesData.add(rule2);
            
            populator.populateSheet("Rules", rulesData);
            
            List<Map<String, Object>> subjectData = new ArrayList<>();
            Map<String, Object> subject1 = new HashMap<>();
            subject1.put("USUBJID", "SUBJ-001");
            subject1.put("Age", 45);
            subject1.put("Eligible", true);
            subjectData.add(subject1);
            
            Map<String, Object> subject2 = new HashMap<>();
            subject2.put("USUBJID", "SUBJ-002");
            subject2.put("Age", 68);
            subject2.put("Eligible", true);
            subjectData.add(subject2);
            
            Map<String, Object> subject3 = new HashMap<>();
            subject3.put("USUBJID", "SUBJ-003");
            subject3.put("Age", 25);
            subject3.put("Eligible", false);
            subjectData.add(subject3);
            
            populator.populateSheet("ELIG_Age_18_75_V1", subjectData);
            
            String outputPath = "output.xlsx";
            populator.saveToFile(outputPath);
            
            System.out.println("Excel file generated successfully: " + outputPath);
            System.out.println("Report Name: " + schema.getReportName());
            System.out.println("Number of sheets: " + schema.getSchema().getSheets().size());
            
            workbook.close();
            
        } catch (IOException e) {
            System.err.println("Error generating Excel file: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
