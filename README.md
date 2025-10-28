# Java JSON Schema Generator

一个用于将 Java 普通对象（POJO）转换为 JSON Schema 格式的工具，专门针对大模型 Tool/Function Calling 的参数定义进行了优化。

## 功能特性

- ✅ 支持基本数据类型映射（String, Integer, Long, Double, Boolean 等）
- ✅ 支持复杂对象和嵌套对象
- ✅ 支持集合类型（List, Set, Array）
- ✅ 支持 Map 类型
- ✅ 支持枚举类型（Enum）
- ✅ 通过 Java 注解支持 schema 元数据
- ✅ 符合 JSON Schema Draft 7 规范
- ✅ 适配大模型 API（OpenAI, Anthropic 等）的 function calling schema 格式

## 快速开始

### 环境要求

- Java 11 或更高版本
- Maven 3.6 或更高版本

### 安装

克隆项目并构建：

```bash
git clone <repository-url>
cd java-jsonschema-generator
mvn clean install
```

### 基本使用

#### 1. 定义 POJO 类

```java
import com.jsonschema.annotations.SchemaDescription;
import com.jsonschema.annotations.SchemaProperty;

@SchemaDescription("A simple user object")
public class User {
    
    @SchemaProperty(required = true)
    @SchemaDescription("User's unique identifier")
    private String id;
    
    @SchemaProperty(required = true, minLength = 2, maxLength = 50)
    @SchemaDescription("User's full name")
    private String name;
    
    @SchemaProperty(format = "email")
    @SchemaDescription("User's email address")
    private String email;
    
    @SchemaProperty(minimum = 0, maximum = 150)
    @SchemaDescription("User's age in years")
    private Integer age;
    
    // Getters and setters...
}
```

#### 2. 生成 JSON Schema

```java
import com.jsonschema.generator.JsonSchemaGenerator;

JsonSchemaGenerator generator = new JsonSchemaGenerator();

// 生成 JSON Schema 字符串
String schemaJson = generator.generateSchemaJson(User.class);
System.out.println(schemaJson);

// 获取 JsonSchema 对象进行程序化操作
JsonSchema schema = generator.generateSchema(User.class);
```

#### 3. 生成 Function Calling Schema

用于 OpenAI 或 Anthropic 等大模型的 function calling：

```java
JsonObject functionSchema = generator.generateSchemaForFunctionCalling(
    User.class,
    "create_user",
    "Create a new user in the system"
);
System.out.println(functionSchema.toString());
```

输出示例：

```json
{
  "name": "create_user",
  "description": "Create a new user in the system",
  "parameters": {
    "type": "object",
    "description": "A simple user object",
    "properties": {
      "id": {
        "type": "string",
        "description": "User's unique identifier"
      },
      "name": {
        "type": "string",
        "description": "User's full name",
        "minLength": 2,
        "maxLength": 50
      },
      "email": {
        "type": "string",
        "description": "User's email address",
        "format": "email"
      },
      "age": {
        "type": "integer",
        "description": "User's age in years",
        "minimum": 0,
        "maximum": 150
      }
    },
    "required": ["id", "name"]
  }
}
```

## 注解说明

### @SchemaDescription

为类或字段添加描述信息。

```java
@SchemaDescription("User's email address")
private String email;
```

### @SchemaProperty

配置字段的 schema 属性：

| 属性 | 类型 | 说明 | 默认值 |
|------|------|------|--------|
| name | String | 自定义字段名称 | 字段名 |
| required | boolean | 是否必填 | false |
| format | String | 格式（如 "email", "date-time"） | "" |
| pattern | String | 正则表达式模式 | "" |
| minimum | double | 数值最小值 | -∞ |
| maximum | double | 数值最大值 | +∞ |
| minLength | int | 字符串最小长度 | -1 |
| maxLength | int | 字符串最大长度 | -1 |
| minItems | int | 数组最小元素数 | -1 |
| maxItems | int | 数组最大元素数 | -1 |
| enumValues | String[] | 枚举值列表 | [] |

使用示例：

```java
@SchemaProperty(
    required = true,
    format = "email",
    pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
)
private String email;

@SchemaProperty(minimum = 0, maximum = 100)
private Integer score;

@SchemaProperty(minLength = 8, maxLength = 128)
private String password;

@SchemaProperty(enumValues = {"active", "inactive", "pending"})
private String status;
```

### @SchemaIgnore

忽略某个字段，不包含在生成的 schema 中。

```java
@SchemaIgnore
private String internalToken;
```

## 支持的数据类型

### 基本类型

| Java 类型 | JSON Schema 类型 |
|-----------|------------------|
| int, Integer, long, Long, short, Short, byte, Byte | integer |
| float, Float, double, Double | number |
| boolean, Boolean | boolean |
| char, Character, String | string |

### 复杂类型

- **对象（Object）**: 自动递归处理嵌套对象
- **数组（Array）**: `String[]`, `int[]` 等
- **集合（Collection）**: `List<T>`, `Set<T>` 等
- **映射（Map）**: `Map<String, T>` 转换为带 `additionalProperties` 的对象
- **枚举（Enum）**: 转换为带 `enum` 约束的字符串类型

### 复杂类型示例

```java
@SchemaDescription("A complex user object")
public class ComplexUser {
    
    // 嵌套对象
    @SchemaDescription("User's primary address")
    private Address primaryAddress;
    
    // List 集合
    @SchemaDescription("List of alternate addresses")
    private List<Address> alternateAddresses;
    
    // Set 集合
    @SchemaDescription("Set of user roles")
    private Set<String> roles;
    
    // Map 类型
    @SchemaDescription("User metadata")
    private Map<String, String> metadata;
    
    // 数组
    @SchemaDescription("User tags")
    private String[] tags;
    
    // 枚举
    @SchemaDescription("User type")
    private UserType userType;
}
```

## 运行测试

```bash
mvn test
```

## 运行示例

```bash
mvn compile exec:java -Dexec.mainClass="com.jsonschema.examples.UsageExample"
```

## 使用场景

### 1. OpenAI Function Calling

```java
JsonSchemaGenerator generator = new JsonSchemaGenerator();
JsonObject functionDef = generator.generateSchemaForFunctionCalling(
    WeatherRequest.class,
    "get_weather",
    "Get the current weather for a location"
);

// 在 OpenAI API 调用中使用
// ChatCompletionRequest.builder()
//     .functions(List.of(functionDef))
//     .build();
```

### 2. Anthropic Claude Tool Use

```java
JsonSchema parametersSchema = generator.generateSchema(SearchRequest.class);
// 转换为 Anthropic 的 tool schema 格式
```

### 3. 自定义 API 文档生成

```java
JsonSchemaGenerator generator = new JsonSchemaGenerator();
Map<String, JsonSchema> apiSchemas = new HashMap<>();

apiSchemas.put("User", generator.generateSchema(User.class));
apiSchemas.put("Product", generator.generateSchema(Product.class));
apiSchemas.put("Order", generator.generateSchema(Order.class));

// 使用这些 schema 生成 API 文档
```

## 项目结构

```
java-jsonschema-generator/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── jsonschema/
│   │               ├── annotations/      # 自定义注解
│   │               │   ├── SchemaDescription.java
│   │               │   ├── SchemaProperty.java
│   │               │   └── SchemaIgnore.java
│   │               ├── generator/        # 核心生成器
│   │               │   └── JsonSchemaGenerator.java
│   │               └── model/            # Schema 模型
│   │                   └── JsonSchema.java
│   └── test/
│       └── java/
│           └── com/
│               └── jsonschema/
│                   ├── examples/         # 示例 POJO
│                   │   ├── SimpleUser.java
│                   │   ├── ComplexUser.java
│                   │   ├── Address.java
│                   │   ├── UserType.java
│                   │   └── UsageExample.java
│                   └── generator/        # 单元测试
│                       └── JsonSchemaGeneratorTest.java
├── pom.xml
└── README.md
```

## 依赖项

- **Gson**: 用于 JSON 序列化和反序列化
- **JUnit 5**: 用于单元测试

## 技术实现

本工具使用 Java 反射 API 来分析 POJO 的结构，并通过自定义注解提供额外的元数据支持。主要特点：

1. **反射分析**: 递归遍历类的字段和类型信息
2. **类型映射**: 将 Java 类型映射到 JSON Schema 类型
3. **泛型支持**: 处理参数化类型（如 `List<T>`, `Map<K,V>`）
4. **循环引用检测**: 防止无限递归
5. **注解驱动**: 通过注解提供丰富的 schema 元数据

## 贡献

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License

## 联系方式

如有问题或建议，请创建 Issue。
