# Excel Schema Generator - Complete Example

## Overview

This example demonstrates how to use the Excel Schema Generator to create Excel files based on your JSON schema.

## Your Original Schema

Based on your provided schema, here's how to use it:

```json
{
    "report_name": "",
    "report_file_type": "excel",
    "description": "",
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
                        "enum_values": [
                            "a",
                            "b"
                        ],
                        "description": "Version of the business",
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
                    }
                ]
            }
        ]
    }
}
```

## Java Code Example

### Step 1: Parse the Schema

```java
import com.excel.schema.parser.SchemaParser;
import com.excel.schema.model.ExcelSchema;

// Parse from JSON string
String schemaJson = "{ ... your schema JSON ... }";
SchemaParser parser = new SchemaParser();
ExcelSchema schema = parser.parseFromJson(schemaJson);

// Or parse from file
ExcelSchema schema = parser.parseFromFile("path/to/schema.json");
```

### Step 2: Generate Excel Structure

```java
import com.excel.schema.generator.ExcelGenerator;
import org.apache.poi.ss.usermodel.Workbook;

// Generate Excel workbook with structure
ExcelGenerator generator = new ExcelGenerator(schema);
Workbook workbook = generator.generate();

// Or generate directly to file (without data)
generator.generateToFile("template.xlsx");
```

### Step 3: Populate Data

```java
import com.excel.schema.generator.ExcelDataPopulator;
import java.util.*;

// Create data populator
ExcelDataPopulator populator = new ExcelDataPopulator(schema, workbook);

// Populate Key-Value sheet (Index)
Map<String, Object> indexData = new HashMap<>();
indexData.put("Study Name", "Clinical Trial Study ABC-123");
populator.populateSheet("Index", indexData);

// Populate Tabular sheet (Eligibility Guardrail)
List<Map<String, Object>> eligibilityData = new ArrayList<>();

Map<String, Object> row1 = new HashMap<>();
row1.put("ELIG_Measurable_Lesion_V1", "a");
eligibilityData.add(row1);

Map<String, Object> row2 = new HashMap<>();
row2.put("ELIG_Measurable_Lesion_V1", "b");
eligibilityData.add(row2);

populator.populateSheet("Eligibility Guardrail", eligibilityData);

// Populate Rules sheet
List<Map<String, Object>> rulesData = new ArrayList<>();
rulesData.add(Map.of("ID", "R001"));
rulesData.add(Map.of("ID", "R002"));
populator.populateSheet("Rules", rulesData);

// Populate ELIG_Age_18_75_V1 sheet
List<Map<String, Object>> subjectData = new ArrayList<>();
subjectData.add(Map.of("USUBJID", "SUBJ-001"));
subjectData.add(Map.of("USUBJID", "SUBJ-002"));
populator.populateSheet("ELIG_Age_18_75_V1", subjectData);

// Save to file
populator.saveToFile("output.xlsx");

// Close workbook
workbook.close();
```

## Complete Working Example

```java
package com.example;

import com.excel.schema.generator.ExcelDataPopulator;
import com.excel.schema.generator.ExcelGenerator;
import com.excel.schema.model.ExcelSchema;
import com.excel.schema.parser.SchemaParser;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.util.*;

public class MyExcelGenerator {
    
    public static void main(String[] args) {
        try {
            // Your schema JSON
            String schemaJson = """
                {
                    "report_name": "Clinical Study Report",
                    "report_file_type": "excel",
                    "description": "Study eligibility report",
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
                                    }
                                ]
                            }
                        ]
                    }
                }
                """;
            
            // Step 1: Parse schema
            SchemaParser parser = new SchemaParser();
            ExcelSchema schema = parser.parseFromJson(schemaJson);
            
            // Step 2: Generate Excel structure
            ExcelGenerator generator = new ExcelGenerator(schema);
            Workbook workbook = generator.generate();
            
            // Step 3: Populate data
            ExcelDataPopulator populator = new ExcelDataPopulator(schema, workbook);
            
            // Populate Index sheet (Key-Value format)
            Map<String, Object> indexData = new HashMap<>();
            indexData.put("Study Name", "Clinical Trial ABC-123");
            populator.populateSheet("Index", indexData);
            
            // Populate Eligibility Guardrail sheet (Tabular format)
            List<Map<String, Object>> eligibilityData = new ArrayList<>();
            eligibilityData.add(Map.of("ELIG_Measurable_Lesion_V1", "a"));
            eligibilityData.add(Map.of("ELIG_Measurable_Lesion_V1", "b"));
            populator.populateSheet("Eligibility Guardrail", eligibilityData);
            
            // Populate Rules sheet
            List<Map<String, Object>> rulesData = new ArrayList<>();
            rulesData.add(Map.of("ID", "R001"));
            rulesData.add(Map.of("ID", "R002"));
            populator.populateSheet("Rules", rulesData);
            
            // Populate ELIG_Age_18_75_V1 sheet
            List<Map<String, Object>> subjectData = new ArrayList<>();
            subjectData.add(Map.of("USUBJID", "SUBJ-001"));
            subjectData.add(Map.of("USUBJID", "SUBJ-002"));
            subjectData.add(Map.of("USUBJID", "SUBJ-003"));
            populator.populateSheet("ELIG_Age_18_75_V1", subjectData);
            
            // Step 4: Save to file
            String outputPath = "clinical_study_report.xlsx";
            populator.saveToFile(outputPath);
            
            System.out.println("Excel file generated successfully: " + outputPath);
            
            // Close workbook
            workbook.close();
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

## Understanding Format Types

### 1. Key-Value Format (`format_type: "key_value"`)

Used for metadata and configuration. Creates a two-column layout:

| Key (Column 1)  | Value (Column 2) |
|-----------------|------------------|
| Study Name      | ABC-123          |
| Study Date      | 2024-01-15       |

**When to use**: 
- Configuration settings
- Metadata
- Report headers
- Single-value properties

### 2. Tabular Format (`format_type: "tabular"`)

Used for traditional table data:

| ID   | Name           | Status  |
|------|----------------|---------|
| R001 | Rule 1         | Active  |
| R002 | Rule 2         | Pending |

**When to use**:
- List of records
- Database-like data
- Multiple rows of related data

## Data Types Supported

| Type    | Java Type           | Example                    |
|---------|---------------------|----------------------------|
| string  | String              | "ABC-123"                  |
| integer | Integer             | 42                         |
| double  | Double              | 3.14                       |
| boolean | Boolean             | true                       |
| date    | Date/LocalDate      | Date object or "2024-01-15"|
| enum    | String (validated)  | "a" or "b"                 |

## Running the Example

1. **Compile the project**:
   ```bash
   mvn clean compile
   ```

2. **Run the demo**:
   ```bash
   mvn exec:java -Dexec.mainClass="com.excel.schema.ExcelSchemaDemo"
   ```

3. **Check the output**:
   ```bash
   ls -lh output.xlsx
   ```

## Expected Excel Output

The generated Excel file will contain 4 sheets:

1. **Index** (Key-Value format)
   - Study Name: Clinical Trial ABC-123

2. **Eligibility Guardrail** (Tabular format)
   - Header: ELIG_Measurable_Lesion_V1 *
   - Row 1: a
   - Row 2: b
   - Cell has dropdown: [a, b]

3. **Rules** (Tabular format)
   - Header: ID *
   - Row 1: R001
   - Row 2: R002

4. **ELIG_Age_18_75_V1** (Tabular format)
   - Header: USUBJID *
   - Row 1: SUBJ-001
   - Row 2: SUBJ-002
   - Row 3: SUBJ-003

## Tips

1. **Required fields** are marked with `*` in tabular headers
2. **Enum types** automatically get dropdown lists
3. **Descriptions** appear as cell comments (hover to view)
4. **Custom labels** can be different from column names
5. **Empty cells** are formatted but contain no data

## Next Steps

- Modify the schema to match your needs
- Add more columns and sheets
- Customize data validation rules
- Integrate with your application

See [README.md](README.md) for full API documentation.
See [USAGE_ZH.md](USAGE_ZH.md) for Chinese documentation.
