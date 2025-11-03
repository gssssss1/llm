# Quick Start Guide

## The Easiest Way - Using ExcelSchemaBuilder

For the fastest way to get started, use the fluent `ExcelSchemaBuilder` API:

```java
import com.excel.schema.ExcelSchemaBuilder;
import java.util.*;

public class QuickStart {
    public static void main(String[] args) throws Exception {
        // Create and populate Excel in one fluent chain
        ExcelSchemaBuilder.fromJson(yourSchemaJson)
            .build()
            .addKeyValueData("Index", Map.of(
                "Study Name", "Clinical Trial ABC-123"
            ))
            .addTabularData("Eligibility Guardrail", List.of(
                Map.of("ELIG_Measurable_Lesion_V1", "a"),
                Map.of("ELIG_Measurable_Lesion_V1", "b")
            ))
            .addTabularData("Rules", List.of(
                Map.of("ID", "R001"),
                Map.of("ID", "R002")
            ))
            .addTabularData("ELIG_Age_18_75_V1", List.of(
                Map.of("USUBJID", "SUBJ-001"),
                Map.of("USUBJID", "SUBJ-002")
            ))
            .saveTo("output.xlsx");
    }
}
```

## Complete Example

```java
package com.example;

import com.excel.schema.ExcelSchemaBuilder;

import java.io.IOException;
import java.util.*;

public class SimpleExcelGenerator {
    
    public static void main(String[] args) {
        String schemaJson = """
            {
                "report_name": "My Report",
                "report_file_type": "excel",
                "description": "",
                "schema": {
                    "sheet": [
                        {
                            "name": "Index",
                            "format_type": "key_value",
                            "columns": [
                                {
                                    "name": "Study Name",
                                    "data_type": "string",
                                    "required": true
                                }
                            ]
                        },
                        {
                            "name": "Data",
                            "format_type": "tabular",
                            "columns": [
                                {
                                    "name": "ID",
                                    "data_type": "string",
                                    "required": true
                                }
                            ]
                        }
                    ]
                }
            }
            """;
        
        try (ExcelSchemaBuilder builder = ExcelSchemaBuilder.fromJson(schemaJson)) {
            builder.build()
                   .addKeyValueData("Index", Map.of(
                       "Study Name", "My Study"
                   ))
                   .addTabularData("Data", List.of(
                       Map.of("ID", "001"),
                       Map.of("ID", "002"),
                       Map.of("ID", "003")
                   ))
                   .saveTo("my_report.xlsx");
            
            System.out.println("Excel file created successfully!");
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

## From File

```java
ExcelSchemaBuilder.fromFile("schema.json")
    .build()
    .addKeyValueData("Index", indexData)
    .addTabularData("Rules", rulesData)
    .saveTo("output.xlsx");
```

## Generate Template Only (No Data)

```java
ExcelGenerator generator = new ExcelGenerator(schema);
generator.generateToFile("template.xlsx");
```

## Key Points

1. **Chain calls** for fluent API
2. **Use Map** for key-value sheets
3. **Use List<Map>** for tabular sheets
4. **Column names** must match schema
5. **Try-with-resources** for auto-close

## Common Patterns

### Pattern 1: Load from Database

```java
List<Map<String, Object>> dbData = loadFromDatabase();

ExcelSchemaBuilder.fromFile("schema.json")
    .build()
    .addTabularData("Results", dbData)
    .saveTo("db_export.xlsx");
```

### Pattern 2: Multiple Sheets

```java
ExcelSchemaBuilder builder = ExcelSchemaBuilder.fromJson(schema);
builder.build()
    .addKeyValueData("Metadata", metadataMap)
    .addTabularData("Sheet1", data1)
    .addTabularData("Sheet2", data2)
    .addTabularData("Sheet3", data3)
    .saveTo("multi_sheet.xlsx");
```

### Pattern 3: Progressive Building

```java
ExcelSchemaBuilder builder = ExcelSchemaBuilder.fromJson(schema).build();

// Add data progressively
builder.addKeyValueData("Config", configData);
Thread.sleep(1000);
builder.addTabularData("Results", results);
Thread.sleep(1000);
builder.addTabularData("Summary", summary);

// Save when ready
builder.saveTo("final.xlsx");
builder.close();
```

## That's It!

You're ready to generate Excel files. For more details, see:
- [README.md](README.md) - Full documentation
- [EXAMPLE.md](EXAMPLE.md) - Complete examples
- [USAGE_ZH.md](USAGE_ZH.md) - Chinese guide
