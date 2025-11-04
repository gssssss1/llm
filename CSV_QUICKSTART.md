# CSV Quick Start / CSV快速开始

## 最简单的方式 - 只需改一行！

### Excel → CSV 只需改变 `report_file_type`

```java
// Excel 格式
String schema = """
{
    "report_name": "My Report",
    "report_file_type": "excel",  // ← Excel格式
    "schema": { ... }
}
""";

// CSV 格式 - 只需改这一行！
String schema = """
{
    "report_name": "My Report",
    "report_file_type": "csv",    // ← CSV格式
    "schema": { ... }
}
""";
```

## 完整示例

```java
import com.excel.schema.UniversalSchemaBuilder;
import java.util.*;

public class QuickCsvExample {
    public static void main(String[] args) throws Exception {
        
        // 定义 CSV Schema（只需设置 report_file_type 为 "csv"）
        String csvSchema = """
            {
                "report_name": "临床研究报告",
                "report_file_type": "csv",
                "description": "CSV格式报告",
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
                                },
                                {
                                    "name": "Name",
                                    "data_type": "string",
                                    "required": true
                                }
                            ]
                        }
                    ]
                }
            }
            """;
        
        // 生成 CSV 文件 - 代码完全一样！
        UniversalSchemaBuilder.fromJson(csvSchema)
            .build()
            .addKeyValueData("Index", Map.of(
                "Study Name", "ABC-123"
            ))
            .addTabularData("Data", List.of(
                Map.of("ID", "001", "Name", "张三"),
                Map.of("ID", "002", "Name", "李四")
            ))
            .saveTo("csv_reports");  // 自动创建目录，包含多个CSV文件
        
        System.out.println("CSV文件已生成!");
    }
}
```

## 输出结果

**Excel 格式输出：**
```
output.xlsx  (单个文件)
```

**CSV 格式输出：**
```
csv_reports/
├── Index.csv
└── Data.csv
```

## 查看生成的 CSV

**Index.csv:**
```csv
Key,Value
Study Name,ABC-123
```

**Data.csv:**
```csv
ID *,Name *
001,张三
002,李四
```

## 运行示例

```bash
# 生成 CSV 报告
mvn exec:java -Dexec.mainClass="com.excel.schema.CsvSchemaDemo"

# 同时生成 Excel 和 CSV
mvn exec:java -Dexec.mainClass="com.excel.schema.UniversalSchemaDemo"

# 只生成 CSV
mvn exec:java -Dexec.mainClass="com.excel.schema.UniversalSchemaDemo" -Dexec.args="csv"

# 只生成 Excel
mvn exec:java -Dexec.mainClass="com.excel.schema.UniversalSchemaDemo" -Dexec.args="excel"
```

## 核心要点

1. ✅ **Schema 不变** - 使用相同的 JSON schema
2. ✅ **代码不变** - 使用相同的 Java 代码  
3. ✅ **只改一处** - 只需修改 `report_file_type` 从 `"excel"` 到 `"csv"`
4. ✅ **自动适配** - UniversalSchemaBuilder 自动选择正确的生成器

## 对比

| 特性 | Excel | CSV |
|-----|-------|-----|
| 文件数量 | 1个 | 每个sheet一个 |
| 格式 | 二进制 | 纯文本 |
| 大小 | 较大 | 小 |
| 样式 | ✅ | ❌ |
| 数据验证 | ✅ | ❌ |
| 打开速度 | 慢 | 快 |
| 版本控制 | ❌ | ✅ |
| 跨平台 | ✅ | ✅ |

## 就这么简单！

修改一行配置，其他都不用改！

Want more details? See [CSV_GUIDE.md](CSV_GUIDE.md)
