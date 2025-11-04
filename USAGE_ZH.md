# Excel Schema Generator 使用指南

## 概述

Excel Schema Generator 是一个强大的Java库，允许您通过JSON Schema定义来生成和填充Excel文件。它支持两种主要的格式类型：

1. **Key-Value（键值对）格式** - 适用于配置、元数据等
2. **Tabular（表格）格式** - 适用于传统的表格数据

## 核心概念

### Schema结构

```
ExcelSchema (根对象)
└── Schema
    └── Sheet[] (多个Sheet)
        └── Column[] (多个列)
```

### 格式类型

#### 1. Key-Value格式 (`format_type: "key_value"`)

显示效果：
```
| 键名          | 值           |
|--------------|-------------|
| Study Name   | ABC-123     |
| Study Date   | 2024-01-15  |
| PI Name      | Dr. Smith   |
```

#### 2. Tabular格式 (`format_type: "tabular"`)

显示效果：
```
| ID    | Name      | Status  |
|-------|-----------|---------|
| R001  | Rule 1    | Active  |
| R002  | Rule 2    | Pending |
```

## 完整示例

### 1. 定义Schema

```json
{
    "report_name": "临床研究报告",
    "report_file_type": "excel",
    "description": "完整的临床研究数据报告",
    "schema": {
        "sheet": [
            {
                "name": "Index",
                "format_type": "key_value",
                "description": "索引页包含研究的元数据",
                "columns": [
                    {
                        "name": "Study Name",
                        "data_type": "string",
                        "description": "研究名称",
                        "required": true
                    },
                    {
                        "name": "Study Date",
                        "data_type": "string",
                        "description": "研究日期",
                        "required": true
                    },
                    {
                        "name": "Principal Investigator",
                        "data_type": "string",
                        "description": "主要研究者姓名",
                        "required": false
                    }
                ]
            },
            {
                "name": "Eligibility Guardrail",
                "format_type": "tabular",
                "description": "资格护栏数据",
                "columns": [
                    {
                        "name": "ELIG_Measurable_Lesion_V1",
                        "data_type": "enum",
                        "label": "可测量病灶V1",
                        "enum_values": ["a", "b"],
                        "description": "业务规则版本",
                        "required": true
                    },
                    {
                        "name": "Status",
                        "data_type": "enum",
                        "label": "状态",
                        "enum_values": ["Active", "Inactive", "Pending"],
                        "description": "当前状态",
                        "required": true
                    }
                ]
            },
            {
                "name": "Rules",
                "format_type": "tabular",
                "description": "验证规则",
                "columns": [
                    {
                        "name": "ID",
                        "data_type": "string",
                        "description": "规则ID",
                        "required": true
                    },
                    {
                        "name": "Rule Name",
                        "data_type": "string",
                        "description": "规则名称",
                        "required": true
                    },
                    {
                        "name": "Severity",
                        "data_type": "enum",
                        "enum_values": ["Error", "Warning", "Info"],
                        "description": "严重程度",
                        "required": false
                    }
                ]
            },
            {
                "name": "ELIG_Age_18_75_V1",
                "description": "年龄资格规则",
                "format_type": "tabular",
                "columns": [
                    {
                        "name": "USUBJID",
                        "data_type": "string",
                        "description": "受试者ID",
                        "required": true
                    },
                    {
                        "name": "Age",
                        "data_type": "integer",
                        "description": "受试者年龄",
                        "required": true
                    },
                    {
                        "name": "Eligible",
                        "data_type": "boolean",
                        "description": "是否符合资格",
                        "required": true
                    }
                ]
            }
        ]
    }
}
```

### 2. Java代码实现

```java
package com.example;

import com.excel.schema.generator.ExcelDataPopulator;
import com.excel.schema.generator.ExcelGenerator;
import com.excel.schema.model.ExcelSchema;
import com.excel.schema.parser.SchemaParser;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.util.*;

public class ClinicalStudyReportGenerator {
    
    public static void main(String[] args) throws IOException {
        // 1. 解析Schema
        SchemaParser parser = new SchemaParser();
        ExcelSchema schema = parser.parseFromFile("schema.json");
        
        // 2. 生成Excel结构
        ExcelGenerator generator = new ExcelGenerator(schema);
        Workbook workbook = generator.generate();
        
        // 3. 创建数据填充器
        ExcelDataPopulator populator = new ExcelDataPopulator(schema, workbook);
        
        // 4. 填充Index（Key-Value格式）
        Map<String, Object> indexData = new HashMap<>();
        indexData.put("Study Name", "Clinical Trial ABC-123");
        indexData.put("Study Date", "2024-01-15");
        indexData.put("Principal Investigator", "Dr. John Smith");
        populator.populateSheet("Index", indexData);
        
        // 5. 填充Eligibility Guardrail（Tabular格式）
        List<Map<String, Object>> eligibilityData = new ArrayList<>();
        
        Map<String, Object> elig1 = new HashMap<>();
        elig1.put("ELIG_Measurable_Lesion_V1", "a");
        elig1.put("Status", "Active");
        eligibilityData.add(elig1);
        
        Map<String, Object> elig2 = new HashMap<>();
        elig2.put("ELIG_Measurable_Lesion_V1", "b");
        elig2.put("Status", "Inactive");
        eligibilityData.add(elig2);
        
        populator.populateSheet("Eligibility Guardrail", eligibilityData);
        
        // 6. 填充Rules
        List<Map<String, Object>> rulesData = new ArrayList<>();
        
        rulesData.add(Map.of(
            "ID", "R001",
            "Rule Name", "Age Validation",
            "Severity", "Error"
        ));
        
        rulesData.add(Map.of(
            "ID", "R002",
            "Rule Name", "Date Range Check",
            "Severity", "Warning"
        ));
        
        rulesData.add(Map.of(
            "ID", "R003",
            "Rule Name", "Data Completeness",
            "Severity", "Info"
        ));
        
        populator.populateSheet("Rules", rulesData);
        
        // 7. 填充受试者数据
        List<Map<String, Object>> subjectData = new ArrayList<>();
        
        subjectData.add(Map.of(
            "USUBJID", "SUBJ-001",
            "Age", 45,
            "Eligible", true
        ));
        
        subjectData.add(Map.of(
            "USUBJID", "SUBJ-002",
            "Age", 68,
            "Eligible", true
        ));
        
        subjectData.add(Map.of(
            "USUBJID", "SUBJ-003",
            "Age", 25,
            "Eligible", false
        ));
        
        subjectData.add(Map.of(
            "USUBJID", "SUBJ-004",
            "Age", 52,
            "Eligible", true
        ));
        
        populator.populateSheet("ELIG_Age_18_75_V1", subjectData);
        
        // 8. 保存文件
        String outputPath = "clinical_study_report.xlsx";
        populator.saveToFile(outputPath);
        
        System.out.println("Excel文件生成成功: " + outputPath);
        System.out.println("报告名称: " + schema.getReportName());
        System.out.println("Sheet数量: " + schema.getSchema().getSheets().size());
        
        // 9. 关闭工作簿
        workbook.close();
    }
}
```

## 常见用例

### 用例1：只生成Excel模板（不填充数据）

```java
SchemaParser parser = new SchemaParser();
ExcelSchema schema = parser.parseFromJson(schemaJson);

ExcelGenerator generator = new ExcelGenerator(schema);
generator.generateToFile("template.xlsx");
```

### 用例2：从JSON字符串生成

```java
String jsonSchema = "{ \"report_name\": \"Test\", ... }";

SchemaParser parser = new SchemaParser();
ExcelSchema schema = parser.parseFromJson(jsonSchema);

ExcelGenerator generator = new ExcelGenerator(schema);
Workbook workbook = generator.generate();
```

### 用例3：动态构建Schema

```java
// 创建列定义
Column column = Column.builder()
    .name("Study Name")
    .dataType(DataType.STRING)
    .description("研究名称")
    .required(true)
    .build();

// 创建Sheet定义
Sheet sheet = Sheet.builder()
    .name("Index")
    .formatType(FormatType.KEY_VALUE)
    .columns(Collections.singletonList(column))
    .build();

// 创建Schema
ExcelSchema.Schema schemaObj = ExcelSchema.Schema.builder()
    .sheets(Collections.singletonList(sheet))
    .build();

ExcelSchema schema = ExcelSchema.builder()
    .reportName("Dynamic Report")
    .reportFileType("excel")
    .schema(schemaObj)
    .build();

// 生成Excel
ExcelGenerator generator = new ExcelGenerator(schema);
generator.generateToFile("dynamic_report.xlsx");
```

### 用例4：批量填充数据

```java
ExcelGenerator generator = new ExcelGenerator(schema);
Workbook workbook = generator.generate();
ExcelDataPopulator populator = new ExcelDataPopulator(schema, workbook);

// 从数据库读取数据
List<Map<String, Object>> dataFromDB = fetchDataFromDatabase();

// 填充到Excel
populator.populateSheet("Data Sheet", dataFromDB);

// 保存
populator.saveToFile("output.xlsx");
workbook.close();
```

## 数据类型支持

| 数据类型 | Java类型 | 说明 |
|---------|---------|------|
| string | String | 字符串 |
| integer | Integer, int | 整数 |
| double | Double, double | 浮点数 |
| boolean | Boolean, boolean | 布尔值 |
| date | Date, LocalDate | 日期 |
| enum | String | 枚举值（自动添加下拉列表） |

## 高级特性

### 1. 枚举类型与数据验证

当列的`data_type`为`enum`时，系统会自动：
- 在单元格创建下拉列表
- 添加数据验证规则
- 显示错误提示

```json
{
    "name": "Status",
    "data_type": "enum",
    "enum_values": ["Active", "Inactive", "Pending"],
    "required": true
}
```

### 2. 必填字段标记

设置`required: true`时：
- Key-Value格式：值单元格会被重点标记
- Tabular格式：表头会显示 * 标记

### 3. 单元格注释

`description`字段会自动转换为Excel单元格注释：
```json
{
    "name": "Study Name",
    "description": "这里输入研究的完整名称"
}
```

用户在Excel中鼠标悬停时会看到提示。

### 4. 自定义标签

使用`label`字段可以设置不同的显示名称：
```json
{
    "name": "ELIG_Measurable_Lesion_V1",
    "label": "可测量病灶V1",
    "data_type": "enum"
}
```

## 最佳实践

1. **Schema设计**
   - 使用有意义的列名
   - 为所有字段添加描述
   - 合理使用枚举类型限制输入

2. **数据填充**
   - 确保数据类型匹配
   - 处理null值
   - 验证必填字段

3. **性能优化**
   - 大批量数据时考虑分批处理
   - 及时关闭Workbook对象
   - 使用try-with-resources

4. **错误处理**
   ```java
   try {
       SchemaParser parser = new SchemaParser();
       ExcelSchema schema = parser.parseFromFile("schema.json");
       // ... 生成Excel
   } catch (IOException e) {
       System.err.println("文件操作错误: " + e.getMessage());
   } catch (IllegalArgumentException e) {
       System.err.println("Schema定义错误: " + e.getMessage());
   }
   ```

## 常见问题

### Q: 如何添加多行数据到Key-Value格式的Sheet?
A: Key-Value格式设计用于配置数据，每个Column对应一行。如需多行数据，使用Tabular格式。

### Q: 枚举值可以是中文吗？
A: 可以，`enum_values`支持任何字符串，包括中文。

### Q: 如何设置单元格样式？
A: 当前版本提供默认样式（表头灰色背景，数据区域带边框）。自定义样式功能可以通过继承`ExcelGenerator`实现。

### Q: 支持公式吗？
A: 当前版本专注于数据填充。公式支持可以在后续版本添加。

### Q: 如何导出大量数据？
A: 对于超大数据集，建议使用Apache POI的SXSSFWorkbook（流式写入）以优化内存使用。

## 总结

Excel Schema Generator提供了一个简洁而强大的方式来生成和管理Excel文件。通过JSON Schema定义，您可以：

✅ 快速创建Excel模板  
✅ 确保数据一致性  
✅ 添加数据验证  
✅ 自动化Excel生成流程  
✅ 减少手工操作错误  

开始使用吧！
