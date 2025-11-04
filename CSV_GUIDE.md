# CSV Report Generation Guide / CSV报告生成指南

## Overview / 概述

The Excel Schema Generator now supports CSV file generation in addition to Excel files. You can generate reports in CSV format by setting `report_file_type` to `"csv"`.

Excel Schema Generator 现在除了支持 Excel 文件外，还支持 CSV 文件生成。您可以通过将 `report_file_type` 设置为 `"csv"` 来生成 CSV 格式的报告。

## Key Differences / 主要区别

### Excel Format / Excel 格式
- Single file with multiple sheets / 单个文件包含多个工作表
- Rich formatting (colors, styles, borders) / 丰富的格式（颜色、样式、边框）
- Data validation (dropdowns) / 数据验证（下拉列表）
- Cell comments / 单元格注释
- File extension: `.xlsx`

### CSV Format / CSV 格式
- Multiple files (one per sheet) / 多个文件（每个工作表一个）
- Plain text format / 纯文本格式
- No formatting or validation / 无格式或验证
- Simple and lightweight / 简单轻量
- File extension: `.csv`

## Schema Definition / Schema 定义

The schema structure remains the same. Only change `report_file_type`:

Schema 结构保持不变，只需更改 `report_file_type`：

```json
{
    "report_name": "Clinical Study Report",
    "report_file_type": "csv",  // ← Change from "excel" to "csv"
    "description": "CSV format report",
    "schema": {
        "sheet": [
            {
                "name": "Index",
                "format_type": "key_value",
                "columns": [...]
            },
            {
                "name": "Data",
                "format_type": "tabular",
                "columns": [...]
            }
        ]
    }
}
```

## Usage Examples / 使用示例

### Method 1: Using UniversalSchemaBuilder (Recommended) / 方法1：使用通用构建器（推荐）

```java
import com.excel.schema.UniversalSchemaBuilder;
import java.util.*;

// Automatically detects format from schema
// 自动从 schema 中检测格式
UniversalSchemaBuilder.fromJson(schemaJson)
    .build()
    .addKeyValueData("Index", Map.of("Study Name", "ABC-123"))
    .addTabularData("Rules", List.of(
        Map.of("ID", "R001", "Name", "Rule 1")
    ))
    .saveTo("csv_output");  // Creates directory with CSV files
                            // 创建包含 CSV 文件的目录
```

### Method 2: Direct CSV Generator / 方法2：直接使用 CSV 生成器

```java
import com.excel.schema.generator.CsvGenerator;
import com.excel.schema.generator.CsvDataPopulator;
import com.excel.schema.model.ExcelSchema;
import com.excel.schema.parser.SchemaParser;

// Parse schema
SchemaParser parser = new SchemaParser();
ExcelSchema schema = parser.parseFromJson(csvSchemaJson);

// Generate CSV structure
CsvGenerator generator = new CsvGenerator(schema);

// Populate with data
CsvDataPopulator populator = new CsvDataPopulator(schema);

// Add key-value data
populator.populateSheet("Index", Map.of(
    "Study Name", "ABC-123",
    "Study Date", "2024-01-15"
));

// Add tabular data
populator.populateSheet("Rules", List.of(
    Map.of("ID", "R001", "Name", "Rule 1"),
    Map.of("ID", "R002", "Name", "Rule 2")
));

// Save to directory (creates one CSV file per sheet)
populator.saveToDirectory("csv_output");

// Or save a single sheet
populator.saveSingleSheet("Index", "index_only.csv");
```

## Output Structure / 输出结构

### Excel Output / Excel 输出
```
output.xlsx
├── Index (sheet)
├── Rules (sheet)
└── Data (sheet)
```

### CSV Output / CSV 输出
```
csv_output/
├── Index.csv
├── Rules.csv
└── Data.csv
```

## CSV File Format / CSV 文件格式

### Key-Value Format / 键值对格式

**Schema:**
```json
{
    "name": "Index",
    "format_type": "key_value",
    "columns": [
        {"name": "Study Name", "data_type": "string"},
        {"name": "Study Date", "data_type": "string"}
    ]
}
```

**Output CSV (Index.csv):**
```csv
Key,Value
Study Name,ABC-123
Study Date,2024-01-15
```

### Tabular Format / 表格格式

**Schema:**
```json
{
    "name": "Rules",
    "format_type": "tabular",
    "columns": [
        {"name": "ID", "data_type": "string", "required": true},
        {"name": "Name", "data_type": "string", "required": true}
    ]
}
```

**Output CSV (Rules.csv):**
```csv
ID *,Name *
R001,Rule 1
R002,Rule 2
```

## Complete Example / 完整示例

```java
package com.example;

import com.excel.schema.UniversalSchemaBuilder;
import java.io.IOException;
import java.util.*;

public class CsvReportExample {
    
    public static void main(String[] args) {
        String csvSchema = """
            {
                "report_name": "Clinical Study",
                "report_file_type": "csv",
                "description": "Study data in CSV format",
                "schema": {
                    "sheet": [
                        {
                            "name": "Metadata",
                            "format_type": "key_value",
                            "columns": [
                                {
                                    "name": "Study Name",
                                    "data_type": "string",
                                    "required": true
                                },
                                {
                                    "name": "Study Date",
                                    "data_type": "string",
                                    "required": true
                                }
                            ]
                        },
                        {
                            "name": "Subjects",
                            "format_type": "tabular",
                            "columns": [
                                {
                                    "name": "Subject ID",
                                    "data_type": "string",
                                    "required": true
                                },
                                {
                                    "name": "Age",
                                    "data_type": "integer",
                                    "required": true
                                },
                                {
                                    "name": "Status",
                                    "data_type": "string",
                                    "required": false
                                }
                            ]
                        }
                    ]
                }
            }
            """;
        
        try {
            UniversalSchemaBuilder.fromJson(csvSchema)
                .build()
                .addKeyValueData("Metadata", Map.of(
                    "Study Name", "Trial XYZ",
                    "Study Date", "2024-01-15"
                ))
                .addTabularData("Subjects", List.of(
                    Map.of("Subject ID", "S001", "Age", 45, "Status", "Active"),
                    Map.of("Subject ID", "S002", "Age", 52, "Status", "Completed"),
                    Map.of("Subject ID", "S003", "Age", 38, "Status", "Active")
                ))
                .saveTo("my_study_csvs");
            
            System.out.println("CSV files generated successfully!");
            System.out.println("Check directory: my_study_csvs/");
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

## Running Examples / 运行示例

### Generate CSV Reports / 生成 CSV 报告

```bash
# Run CSV demo
mvn exec:java -Dexec.mainClass="com.excel.schema.CsvSchemaDemo"

# Run universal demo (both Excel and CSV)
mvn exec:java -Dexec.mainClass="com.excel.schema.UniversalSchemaDemo"

# Run universal demo for CSV only
mvn exec:java -Dexec.mainClass="com.excel.schema.UniversalSchemaDemo" -Dexec.args="csv"

# Run universal demo for Excel only
mvn exec:java -Dexec.mainClass="com.excel.schema.UniversalSchemaDemo" -Dexec.args="excel"
```

## CSV Features / CSV 特性

### Supported / 支持的功能
✅ Key-value format / 键值对格式  
✅ Tabular format / 表格格式  
✅ All data types / 所有数据类型  
✅ Required field markers (*) / 必填字段标记 (*)  
✅ CSV escaping (commas, quotes) / CSV 转义（逗号、引号）  
✅ Custom separators / 自定义分隔符  
✅ UTF-8 encoding / UTF-8 编码  
✅ Multiple sheets (multiple files) / 多个工作表（多个文件）

### Not Supported / 不支持的功能
❌ Cell styling / 单元格样式  
❌ Data validation dropdowns / 数据验证下拉列表  
❌ Cell comments / 单元格注释  
❌ Formulas / 公式  
❌ Merged cells / 合并单元格

## Custom Separator / 自定义分隔符

By default, CSV uses comma (`,`) as separator. You can customize:

默认情况下，CSV 使用逗号（`,`）作为分隔符。您可以自定义：

```java
// Use semicolon for European format
// 使用分号（欧洲格式）
CsvGenerator generator = new CsvGenerator(schema, ";", true);
CsvDataPopulator populator = new CsvDataPopulator(schema, ";");
```

## Best Practices / 最佳实践

### When to Use CSV / 何时使用 CSV

✅ **Use CSV when:** / **使用 CSV 当：**
- Simple data export / 简单数据导出
- Integration with other systems / 与其他系统集成
- Large datasets (better performance) / 大数据集（性能更好）
- Plain text processing needed / 需要纯文本处理
- Version control friendly / 版本控制友好

✅ **Use Excel when:** / **使用 Excel 当：**
- Rich formatting required / 需要丰富格式
- Data validation needed / 需要数据验证
- Single file preferred / 首选单个文件
- Cell comments important / 单元格注释重要
- End-user editing / 最终用户编辑

### File Naming / 文件命名

Sheet names are automatically sanitized for file names:

工作表名称会自动转换为安全的文件名：

- Spaces → Underscores / 空格 → 下划线
- Special characters → Underscores / 特殊字符 → 下划线
- Example: `"Eligibility Guardrail"` → `"Eligibility_Guardrail.csv"`

### Character Encoding / 字符编码

All CSV files are generated with **UTF-8 encoding**, supporting international characters including Chinese.

所有 CSV 文件都使用 **UTF-8 编码**生成，支持包括中文在内的国际字符。

## Testing / 测试

Run CSV-specific tests:

运行 CSV 特定的测试：

```bash
mvn test -Dtest=CsvGeneratorTest
```

## Migration Guide / 迁移指南

### From Excel to CSV / 从 Excel 到 CSV

To convert existing Excel reports to CSV:

将现有 Excel 报告转换为 CSV：

1. Change `report_file_type` from `"excel"` to `"csv"` in schema
2. Change `saveTo()` from file path to directory path
3. Code remains the same!

1. 在 schema 中将 `report_file_type` 从 `"excel"` 改为 `"csv"`
2. 将 `saveTo()` 从文件路径改为目录路径
3. 代码保持不变！

```java
// Before (Excel)
builder.saveTo("output.xlsx");

// After (CSV)
builder.saveTo("output_dir");  // Creates directory with CSV files
```

## Troubleshooting / 故障排除

### Issue: CSV file not created / 问题：CSV 文件未创建

**Solution:** Ensure the output directory exists or will be created

**解决方案：** 确保输出目录存在或将被创建

```java
Files.createDirectories(Paths.get("output_dir"));
```

### Issue: Special characters garbled / 问题：特殊字符乱码

**Solution:** CSV files use UTF-8 encoding. Ensure your CSV reader supports UTF-8.

**解决方案：** CSV 文件使用 UTF-8 编码。确保您的 CSV 阅读器支持 UTF-8。

### Issue: Commas breaking columns / 问题：逗号破坏列

**Solution:** Values with commas are automatically quoted. This is standard CSV behavior.

**解决方案：** 包含逗号的值会自动加引号。这是标准的 CSV 行为。

Example: `"Text, with comma"` → `"Text, with comma"`

## Summary / 总结

The CSV support provides:

CSV 支持提供：

- **Flexibility** - Generate either Excel or CSV from same schema / **灵活性** - 从同一 schema 生成 Excel 或 CSV
- **Simplicity** - Plain text format, easy to process / **简洁性** - 纯文本格式，易于处理
- **Compatibility** - Works with any system that reads CSV / **兼容性** - 与任何读取 CSV 的系统兼容
- **Performance** - Faster for large datasets / **性能** - 大数据集更快

Choose the format that best fits your needs!

选择最适合您需求的格式！
