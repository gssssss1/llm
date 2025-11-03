# Excel Schema Generator

> A powerful Java library for generating Excel files from JSON schema definitions. Supports both Key-Value and Tabular format types.

一个基于JSON Schema定义生成Excel文件的Java库。支持两种格式类型：Key-Value（键值对）和 Tabular（表格）。

[中文使用指南 / Chinese Documentation](USAGE_ZH.md)

## Features / 功能特性

- 📊 **Flexible Schema Definition**: Define Excel structure using JSON / **灵活的Schema定义**: 使用JSON定义Excel文件结构
- 🔑 **Key-Value Format**: Support for configuration and metadata sheets / **Key-Value格式**: 支持键值对格式的sheet
- 📋 **Tabular Format**: Support for traditional table data / **Tabular格式**: 支持传统表格格式的sheet
- ✅ **Data Validation**: Dropdown lists for enum types / **数据验证**: 支持枚举类型的下拉列表
- 💬 **Cell Comments**: Auto-generated descriptions / **注释说明**: 自动为列添加描述性注释
- 🎨 **Custom Styling**: Professional header and data cell styles / **样式设置**: 自动应用表头和数据单元格样式
- 🔒 **Required Fields**: Mark required fields with asterisks / **必填字段**: 标记必填字段（在表头添加 * 标记）

## Tech Stack / 技术栈

- Java 17+
- Apache POI 5.2.3 (Excel文件操作)
- Jackson 2.15.2 (JSON解析)
- Lombok 1.18.28 (减少样板代码)
- Maven (项目管理)

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.excel.schema</groupId>
    <artifactId>excel-schema-generator</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 定义Schema

```json
{
    "report_name": "Clinical Study Report",
    "report_file_type": "excel",
    "description": "临床研究报告",
    "schema": {
        "sheet": [
            {
                "name": "Index",
                "format_type": "key_value",
                "description": "索引页包含任务的元数据",
                "columns": [
                    {
                        "name": "Study Name",
                        "data_type": "string",
                        "description": "研究名称",
                        "required": true
                    }
                ]
            },
            {
                "name": "Eligibility Guardrail",
                "format_type": "tabular",
                "description": "资格护栏",
                "columns": [
                    {
                        "name": "ELIG_Measurable_Lesion_V1",
                        "data_type": "enum",
                        "label": "ELIG_Measurable_Lesion_V1",
                        "enum_values": ["a", "b"],
                        "description": "业务版本",
                        "required": true
                    }
                ]
            }
        ]
    }
}
```

### 3. 使用代码生成Excel

```java
import com.excel.schema.parser.SchemaParser;
import com.excel.schema.generator.ExcelGenerator;
import com.excel.schema.generator.ExcelDataPopulator;
import com.excel.schema.model.ExcelSchema;
import org.apache.poi.ss.usermodel.Workbook;

// 解析Schema
SchemaParser parser = new SchemaParser();
ExcelSchema schema = parser.parseFromJson(schemaJson);

// 生成Excel结构
ExcelGenerator generator = new ExcelGenerator(schema);
Workbook workbook = generator.generate();

// 填充数据
ExcelDataPopulator populator = new ExcelDataPopulator(schema, workbook);

// 填充Key-Value格式的数据
Map<String, Object> indexData = new HashMap<>();
indexData.put("Study Name", "Clinical Trial ABC-123");
populator.populateSheet("Index", indexData);

// 填充Tabular格式的数据
List<Map<String, Object>> tabularData = new ArrayList<>();
Map<String, Object> row1 = new HashMap<>();
row1.put("ELIG_Measurable_Lesion_V1", "a");
tabularData.add(row1);
populator.populateSheet("Eligibility Guardrail", tabularData);

// 保存文件
populator.saveToFile("output.xlsx");
workbook.close();
```

## Schema格式说明

### 根对象

| 字段 | 类型 | 描述 |
|------|------|------|
| report_name | string | 报告名称 |
| report_file_type | string | 文件类型（固定为"excel"） |
| description | string | 报告描述 |
| schema | Schema | Schema定义 |

### Schema对象

| 字段 | 类型 | 描述 |
|------|------|------|
| sheet | Sheet[] | Sheet定义数组 |

### Sheet对象

| 字段 | 类型 | 描述 | 必填 |
|------|------|------|------|
| name | string | Sheet名称 | ✓ |
| format_type | string | 格式类型："key_value" 或 "tabular" | ✓ |
| description | string | Sheet描述 | - |
| columns | Column[] | 列定义数组 | ✓ |

### Column对象

| 字段 | 类型 | 描述 | 必填 |
|------|------|------|------|
| name | string | 列名（用于数据填充） | ✓ |
| data_type | string | 数据类型：string, integer, double, boolean, date, enum | ✓ |
| label | string | 显示标签（用于表格表头） | - |
| description | string | 列描述（将显示为单元格注释） | - |
| required | boolean | 是否必填 | - |
| enum_values | string[] | 枚举值列表（当data_type为enum时使用） | - |

### 格式类型说明

#### key_value格式
- 适用于配置信息、元数据等键值对数据
- 第一列为键（Key），第二列为值（Value）
- 每个Column定义对应一行数据

#### tabular格式
- 适用于传统的表格数据
- 第一行为表头，后续行为数据行
- 每个Column定义对应一列
- 自动生成10行空白数据行

## 项目结构

```
src/main/java/com/excel/schema/
├── model/                          # 数据模型
│   ├── ExcelSchema.java           # Excel Schema根对象
│   ├── Sheet.java                 # Sheet定义
│   ├── Column.java                # 列定义
│   ├── FormatType.java            # 格式类型枚举
│   └── DataType.java              # 数据类型枚举
├── parser/                         # Schema解析器
│   └── SchemaParser.java          # JSON Schema解析器
├── generator/                      # Excel生成器
│   ├── ExcelGenerator.java        # Excel结构生成器
│   └── ExcelDataPopulator.java    # Excel数据填充器
└── ExcelSchemaDemo.java           # 示例代码
```

## 运行示例

### 编译项目
```bash
mvn clean compile
```

### 运行Demo
```bash
mvn exec:java -Dexec.mainClass="com.excel.schema.ExcelSchemaDemo"
```

### 运行测试
```bash
mvn test
```

### 打包
```bash
mvn clean package
```

## API使用示例

### 从文件解析Schema

```java
SchemaParser parser = new SchemaParser();
ExcelSchema schema = parser.parseFromFile("schema.json");
```

### 直接生成Excel文件

```java
ExcelGenerator generator = new ExcelGenerator(schema);
generator.generateToFile("output.xlsx");
```

### 生成并填充数据

```java
// 生成Excel结构
ExcelGenerator generator = new ExcelGenerator(schema);
Workbook workbook = generator.generate();

// 创建数据填充器
ExcelDataPopulator populator = new ExcelDataPopulator(schema, workbook);

// 填充Key-Value类型的Sheet
Map<String, Object> keyValueData = Map.of(
    "Study Name", "ABC-123",
    "Study Date", "2024-01-15"
);
populator.populateSheet("Index", keyValueData);

// 填充Tabular类型的Sheet
List<Map<String, Object>> tabularData = List.of(
    Map.of("ID", "R001", "Rule Name", "Age Validation"),
    Map.of("ID", "R002", "Rule Name", "Date Check")
);
populator.populateSheet("Rules", tabularData);

// 保存文件
populator.saveToFile("output.xlsx");
```

## 支持的数据类型

- **string**: 字符串类型
- **integer**: 整数类型
- **double**: 浮点数类型
- **boolean**: 布尔类型
- **date**: 日期类型
- **enum**: 枚举类型（带下拉列表验证）

## 特性说明

### 数据验证
对于`data_type`为`enum`的列，系统会自动：
- 创建数据验证规则
- 添加下拉列表
- 限制输入值为预定义的枚举值

### 单元格注释
每个列的`description`字段会自动转换为Excel单元格注释，鼠标悬停即可查看。

### 样式设置
- 表头：灰色背景，粗体字体
- 数据单元格：带边框的普通样式
- 必填字段：表头添加 * 标记

## 许可证

MIT License

## 贡献

欢迎提交Issue和Pull Request！

## 联系方式

如有问题，请提交Issue。
