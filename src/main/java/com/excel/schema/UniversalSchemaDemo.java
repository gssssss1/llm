package com.excel.schema;

import com.excel.schema.model.ExcelSchema;
import com.excel.schema.parser.SchemaParser;

import java.io.IOException;
import java.util.*;

public class UniversalSchemaDemo {
    
    public static void main(String[] args) {
        if (args.length > 0 && "csv".equalsIgnoreCase(args[0])) {
            generateCsvReport();
        } else if (args.length > 0 && "excel".equalsIgnoreCase(args[0])) {
            generateExcelReport();
        } else {
            System.out.println("Generating both Excel and CSV reports...\n");
            generateExcelReport();
            System.out.println();
            generateCsvReport();
        }
    }
    
    private static void generateExcelReport() {
        try {
            System.out.println("=== Generating Excel Report ===");
            
            String excelSchema = createSchemaJson("excel");
            
            UniversalSchemaBuilder.fromJson(excelSchema)
                .build()
                .addKeyValueData("Index", createIndexData())
                .addTabularData("Eligibility Guardrail", createEligibilityData())
                .addTabularData("Rules", createRulesData())
                .addTabularData("ELIG_Age_18_75_V1", createSubjectData())
                .saveTo("output_universal.xlsx");
            
            System.out.println("✓ Excel file generated: output_universal.xlsx");
            
        } catch (IOException e) {
            System.err.println("Error generating Excel report: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void generateCsvReport() {
        try {
            System.out.println("=== Generating CSV Report ===");
            
            String csvSchema = createSchemaJson("csv");
            
            UniversalSchemaBuilder.fromJson(csvSchema)
                .build()
                .addKeyValueData("Index", createIndexData())
                .addTabularData("Eligibility Guardrail", createEligibilityData())
                .addTabularData("Rules", createRulesData())
                .addTabularData("ELIG_Age_18_75_V1", createSubjectData())
                .saveTo("csv_output_universal");
            
            System.out.println("✓ CSV files generated in directory: csv_output_universal/");
            System.out.println("  - Index.csv");
            System.out.println("  - Eligibility_Guardrail.csv");
            System.out.println("  - Rules.csv");
            System.out.println("  - ELIG_Age_18_75_V1.csv");
            
        } catch (IOException e) {
            System.err.println("Error generating CSV report: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String createSchemaJson(String fileType) {
        return String.format("""
            {
                "report_name": "Clinical Study Report",
                "report_file_type": "%s",
                "description": "Universal report supporting both Excel and CSV",
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
                                }
                            ]
                        },
                        {
                            "name": "Eligibility Guardrail",
                            "format_type": "tabular",
                            "description": "Eligibility criteria",
                            "columns": [
                                {
                                    "name": "ELIG_Measurable_Lesion_V1",
                                    "data_type": "enum",
                                    "label": "Measurable Lesion",
                                    "enum_values": ["a", "b"],
                                    "description": "Version of the business",
                                    "required": true
                                },
                                {
                                    "name": "Status",
                                    "data_type": "string",
                                    "required": true
                                }
                            ]
                        },
                        {
                            "name": "Rules",
                            "format_type": "tabular",
                            "description": "Validation rules",
                            "columns": [
                                {
                                    "name": "ID",
                                    "data_type": "string",
                                    "required": true
                                },
                                {
                                    "name": "Rule Name",
                                    "data_type": "string",
                                    "required": true
                                }
                            ]
                        },
                        {
                            "name": "ELIG_Age_18_75_V1",
                            "format_type": "tabular",
                            "description": "Age eligibility data",
                            "columns": [
                                {
                                    "name": "USUBJID",
                                    "data_type": "string",
                                    "required": true
                                },
                                {
                                    "name": "Age",
                                    "data_type": "integer",
                                    "required": true
                                },
                                {
                                    "name": "Eligible",
                                    "data_type": "boolean",
                                    "required": true
                                }
                            ]
                        }
                    ]
                }
            }
            """, fileType);
    }
    
    private static Map<String, Object> createIndexData() {
        Map<String, Object> data = new HashMap<>();
        data.put("Study Name", "Clinical Trial ABC-123");
        data.put("Study Date", "2024-01-15");
        return data;
    }
    
    private static List<Map<String, Object>> createEligibilityData() {
        List<Map<String, Object>> data = new ArrayList<>();
        data.add(Map.of("ELIG_Measurable_Lesion_V1", "a", "Status", "Active"));
        data.add(Map.of("ELIG_Measurable_Lesion_V1", "b", "Status", "Inactive"));
        return data;
    }
    
    private static List<Map<String, Object>> createRulesData() {
        List<Map<String, Object>> data = new ArrayList<>();
        data.add(Map.of("ID", "R001", "Rule Name", "Age Validation"));
        data.add(Map.of("ID", "R002", "Rule Name", "Date Range Check"));
        return data;
    }
    
    private static List<Map<String, Object>> createSubjectData() {
        List<Map<String, Object>> data = new ArrayList<>();
        data.add(Map.of("USUBJID", "SUBJ-001", "Age", 45, "Eligible", true));
        data.add(Map.of("USUBJID", "SUBJ-002", "Age", 68, "Eligible", true));
        data.add(Map.of("USUBJID", "SUBJ-003", "Age", 25, "Eligible", false));
        return data;
    }
}
