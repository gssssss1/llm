# LLM Tool Executor - 使用指南

## 概述

LLM Tool Executor 是一个强大的工具执行系统，允许你通过简单的注解定义可被 LLM（大语言模型）调用的工具（Tools/Functions），自动生成符合 OpenAI、Anthropic 等 LLM API 规范的 JSON Schema，并支持动态执行这些工具。

## 核心特性

✅ **注解驱动**: 使用 `@Tool` 注解定义工具方法  
✅ **自动 Schema 生成**: 基于 Java JSON Schema Generator 自动生成参数定义  
✅ **动态执行**: 根据 LLM 返回的参数动态调用对应的工具  
✅ **多 LLM 支持**: 兼容 OpenAI、Anthropic Claude 等主流 LLM API  
✅ **类型安全**: 使用强类型 Java 对象定义参数，编译时检查  
✅ **灵活参数**: 支持复杂对象、基本类型、无参数方法  

## 快速开始

### 1. 定义参数类

使用 JSON Schema 注解定义工具的参数：

```java
import com.jsonschema.annotations.SchemaDescription;
import com.jsonschema.annotations.SchemaProperty;

@SchemaDescription("Weather query parameters")
public class WeatherRequest {
    
    @SchemaProperty(required = true)
    @SchemaDescription("The city name to get weather for")
    private String city;
    
    @SchemaDescription("Country code (optional)")
    private String country;
    
    @SchemaProperty(enumValues = {"celsius", "fahrenheit"})
    @SchemaDescription("Temperature unit")
    private String unit = "celsius";
    
    // Getters and setters...
}
```

### 2. 定义工具方法

使用 `@Tool` 注解标记方法：

```java
import com.jsonschema.annotations.Tool;

public class WeatherTools {
    
    @Tool(description = "Get current weather information for a specified city")
    public String getWeather(WeatherRequest request) {
        // 实现获取天气的逻辑
        return "Weather in " + request.getCity() + ": Sunny, 25°C";
    }
    
    @Tool(description = "Get the current date and time")
    public String getCurrentTime() {
        // 无参数的工具
        return LocalDateTime.now().toString();
    }
}
```

### 3. 注册和使用工具

```java
import com.jsonschema.tool.ToolExecutor;

// 创建执行器
ToolExecutor executor = new ToolExecutor();

// 注册工具
WeatherTools weatherTools = new WeatherTools();
executor.registerTool(weatherTools);

// 获取所有工具的 Schema（发送给 LLM）
List<JsonObject> schemas = executor.getAllToolSchemas();

// 执行工具（根据 LLM 的返回）
String params = "{\"city\":\"Beijing\",\"unit\":\"celsius\"}";
Object result = executor.executeTool("getWeather", params);
```

## 完整示例

### 示例 1: 搜索工具

```java
// 参数定义
@SchemaDescription("Search query parameters")
public class SearchRequest {
    @SchemaProperty(required = true, minLength = 1)
    @SchemaDescription("The search query string")
    private String query;
    
    @SchemaProperty(minimum = 1, maximum = 100)
    @SchemaDescription("Maximum number of results")
    private Integer limit = 10;
    
    // Getters and setters...
}

// 工具实现
public class SearchTools {
    @Tool(name = "search", description = "Search for information in the database")
    public Map<String, Object> search(SearchRequest request) {
        Map<String, Object> result = new HashMap<>();
        result.put("query", request.getQuery());
        result.put("results", performSearch(request.getQuery()));
        return result;
    }
}
```

### 示例 2: 计算工具

```java
// 参数定义
@SchemaDescription("Parameters for calculation")
public class CalculationRequest {
    @SchemaProperty(required = true)
    @SchemaDescription("First number")
    private double a;
    
    @SchemaProperty(required = true)
    @SchemaDescription("Second number")
    private double b;
    
    @SchemaProperty(required = true, enumValues = {"add", "subtract", "multiply", "divide"})
    @SchemaDescription("Operation to perform")
    private String operation;
    
    // Getters and setters...
}

// 工具实现
public class MathTools {
    @Tool(name = "calculate", description = "Perform a calculation")
    public double calculate(CalculationRequest request) {
        switch (request.getOperation()) {
            case "add": return request.getA() + request.getB();
            case "subtract": return request.getA() - request.getB();
            case "multiply": return request.getA() * request.getB();
            case "divide": return request.getA() / request.getB();
            default: throw new IllegalArgumentException("Unknown operation");
        }
    }
}
```

## 与 OpenAI 集成

### 1. 生成 Function Calling Schema

```java
ToolExecutor executor = new ToolExecutor();
executor.registerTool(new WeatherTools());

// 获取所有工具的 Schema
List<JsonObject> toolSchemas = executor.getAllToolSchemas();

// 构建 OpenAI 请求
JsonObject request = new JsonObject();
request.addProperty("model", "gpt-4");

JsonArray messages = new JsonArray();
JsonObject message = new JsonObject();
message.addProperty("role", "user");
message.addProperty("content", "What's the weather in Paris?");
messages.add(message);

request.add("messages", messages);

// 添加工具定义
JsonArray tools = new JsonArray();
for (JsonObject schema : toolSchemas) {
    tools.add(schema);
}
request.add("tools", tools);

// 发送请求到 OpenAI API
// String response = openAIClient.chat(request);
```

### 2. 处理 OpenAI 响应

```java
// 解析 OpenAI 的响应
JsonObject response = // ... 从 API 获取
JsonObject toolCall = response
    .getAsJsonArray("choices")
    .get(0).getAsJsonObject()
    .getAsJsonObject("message")
    .getAsJsonArray("tool_calls")
    .get(0).getAsJsonObject();

String toolName = toolCall.getAsJsonObject("function").get("name").getAsString();
String arguments = toolCall.getAsJsonObject("function").get("arguments").getAsString();

// 执行工具
Object result = executor.executeTool(toolName, arguments);

// 将结果返回给 LLM
JsonObject toolMessage = new JsonObject();
toolMessage.addProperty("role", "tool");
toolMessage.addProperty("tool_call_id", toolCall.get("id").getAsString());
toolMessage.addProperty("content", result.toString());
```

## 与 Anthropic Claude 集成

Claude 的 Tool Use 格式略有不同：

```java
ToolExecutor executor = new ToolExecutor();
executor.registerTools(new WeatherTools(), new SearchTools());

// 构建 Claude 请求
JsonObject request = new JsonObject();
request.addProperty("model", "claude-3-opus-20240229");
request.addProperty("max_tokens", 1024);

JsonArray messages = new JsonArray();
JsonObject message = new JsonObject();
message.addProperty("role", "user");
message.addProperty("content", "What's the weather in London?");
messages.add(message);
request.add("messages", messages);

// 转换为 Claude 的 tools 格式
JsonArray tools = new JsonArray();
for (JsonObject schema : executor.getAllToolSchemas()) {
    JsonObject tool = new JsonObject();
    tool.addProperty("name", schema.get("name").getAsString());
    tool.addProperty("description", schema.get("description").getAsString());
    tool.add("input_schema", schema.getAsJsonObject("parameters"));
    tools.add(tool);
}
request.add("tools", tools);
```

## API 参考

### ToolExecutor

| 方法 | 说明 |
|------|------|
| `registerTool(Object)` | 注册单个工具提供者 |
| `registerTools(Object...)` | 注册多个工具提供者 |
| `executeTool(String name, String json)` | 执行工具（JSON 字符串参数） |
| `executeTool(String name, JsonObject json)` | 执行工具（JsonObject 参数） |
| `executeTool(String name, Map params)` | 执行工具（Map 参数） |
| `executeTool(String name)` | 执行无参数工具 |
| `getAllToolSchemas()` | 获取所有工具的 Schema |
| `getToolSchema(String name)` | 获取指定工具的 Schema |
| `getToolNames()` | 获取所有工具名称 |
| `getToolDefinition(String name)` | 获取工具定义详情 |
| `getToolCount()` | 获取工具数量 |

### @Tool 注解

| 属性 | 类型 | 说明 | 默认值 |
|------|------|------|--------|
| name | String | 工具名称 | 方法名 |
| description | String | 工具描述（必填） | - |

### ToolDefinition

包含工具的完整元数据：
- `name`: 工具名称
- `description`: 工具描述
- `method`: Java 反射方法对象
- `instance`: 工具实例对象
- `parameterClass`: 参数类型
- `schema`: JSON Schema 定义

## 高级用法

### 多个工具提供者

```java
ToolExecutor executor = new ToolExecutor();

// 注册多个工具类
executor.registerTools(
    new WeatherTools(),
    new SearchTools(),
    new CalculatorTools(),
    new DatabaseTools()
);

System.out.println("Registered " + executor.getToolCount() + " tools");
```

### 自定义工具名称

```java
@Tool(name = "custom_search", description = "Custom search tool")
public List<String> mySearchMethod(SearchRequest request) {
    // 实现
}
```

### 错误处理

```java
try {
    Object result = executor.executeTool("getWeather", params);
} catch (IllegalArgumentException e) {
    // 工具不存在
    System.err.println("Tool not found: " + e.getMessage());
} catch (Exception e) {
    // 执行错误
    System.err.println("Tool execution failed: " + e.getMessage());
}
```

### 检查工具是否存在

```java
if (executor.getToolNames().contains("getWeather")) {
    Object result = executor.executeTool("getWeather", params);
}
```

## 运行示例

项目包含完整的示例代码：

```bash
# 运行基本示例
mvn test-compile
java -cp "target/test-classes:target/classes:..." com.jsonschema.tool.ToolExecutorExample

# 运行 LLM 集成示例
java -cp "target/test-classes:target/classes:..." com.jsonschema.tool.LLMIntegrationExample

# 运行测试
mvn test -Dtest=ToolExecutorTest
```

## 最佳实践

### 1. 参数类设计
- 使用描述性的类名和字段名
- 添加详细的 `@SchemaDescription` 注解
- 合理使用 `@SchemaProperty` 的约束（required, min, max, enum 等）
- 为可选参数提供合理的默认值

### 2. 工具方法设计
- 工具方法应该是独立的、无状态的
- 返回类型应该是可序列化的（String, Map, List, POJO 等）
- 避免抛出未处理的异常
- 使用有意义的工具名称和描述

### 3. 错误处理
- 在工具方法中进行参数验证
- 捕获并转换异常为有意义的错误消息
- 返回结构化的错误信息而不是抛出异常

### 4. 性能优化
- 重用 ToolExecutor 实例
- 对于频繁调用的工具，考虑缓存结果
- 异步执行长时间运行的工具

## 示例项目结构

```
src/
├── main/java/com/jsonschema/
│   ├── annotations/
│   │   └── Tool.java                    # @Tool 注解
│   └── tool/
│       ├── ToolDefinition.java          # 工具定义
│       └── ToolExecutor.java            # 工具执行器
└── test/java/com/jsonschema/
    └── tool/
        ├── WeatherRequest.java          # 天气查询参数
        ├── SearchRequest.java           # 搜索参数
        ├── CalculationRequest.java      # 计算参数
        ├── ExampleTools.java            # 示例工具实现
        ├── ToolExecutorTest.java        # 单元测试
        ├── ToolExecutorExample.java     # 基本示例
        └── LLMIntegrationExample.java   # LLM 集成示例
```

## 常见问题

**Q: 工具方法必须有参数吗？**  
A: 不是。工具方法可以没有参数，如 `getCurrentTime()` 示例所示。

**Q: 支持哪些返回类型？**  
A: 支持所有可 JSON 序列化的类型：String, Number, Boolean, Map, List, POJO 等。

**Q: 如何处理复杂的嵌套参数？**  
A: 使用嵌套的 POJO 类，JSON Schema Generator 会自动处理嵌套结构。

**Q: 可以在运行时动态添加工具吗？**  
A: 可以，随时调用 `registerTool()` 或 `registerTools()` 添加新工具。

**Q: 工具执行是线程安全的吗？**  
A: ToolExecutor 本身是线程安全的，但工具方法的线程安全性取决于具体实现。

## 总结

LLM Tool Executor 提供了一种优雅的方式来定义和执行 LLM 工具：

1. **简单**: 使用注解定义工具，无需手写 JSON Schema
2. **类型安全**: 编译时检查参数类型
3. **灵活**: 支持各种参数类型和 LLM 平台
4. **强大**: 自动处理序列化、反序列化和方法调用

现在你可以轻松地为你的 LLM 应用添加自定义工具！
