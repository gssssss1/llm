# Tool 拦截器和 LLM Message 模型

## 新增功能

### 1. Tool 拦截器系统 ⭐
为 Tool 执行添加前置和后置处理能力，支持全局和方法级别的拦截器配置。

### 2. LLM Message 领域模型 ⭐
完整的消息模型，支持 OpenAI 和 Anthropic 格式的消息构建和转换。

---

## 一、Tool 拦截器系统

### 概述

拦截器系统允许你在工具执行前后插入自定义逻辑，例如：
- 日志记录
- 性能监控
- 参数验证
- 权限检查
- 错误处理

### 核心接口

#### ToolInterceptor

```java
public interface ToolInterceptor {
    default void before(ToolInvocation invocation) throws Exception {
        // 工具执行前调用
    }
    
    default void after(ToolInvocation invocation, Object result) throws Exception {
        // 工具执行后调用
    }
    
    default void onError(ToolInvocation invocation, Exception error) throws Exception {
        // 工具执行出错时调用
    }
}
```

#### ToolInvocation

包含工具调用的上下文信息：

```java
public class ToolInvocation {
    String getToolName()              // 工具名称
    Method getMethod()                // 被调用的方法
    Object getInstance()              // 工具实例
    Object[] getArgs()                // 方法参数
    Map<String, Object> getContext()  // 上下文数据
    long getStartTime()               // 开始时间
    long getElapsedTime()             // 已执行时长
    void setAttribute(String key, Object value)  // 设置上下文属性
    Object getAttribute(String key)   // 获取上下文属性
}
```

### 内置拦截器

#### 1. LoggingInterceptor - 日志拦截器

记录工具调用的详细信息：

```java
public class LoggingInterceptor implements ToolInterceptor {
    @Override
    public void before(ToolInvocation invocation) {
        System.out.println("[TOOL] Calling: " + invocation.getToolName());
    }
    
    @Override
    public void after(ToolInvocation invocation, Object result) {
        System.out.println("[TOOL] Completed in " + invocation.getElapsedTime() + "ms");
    }
}
```

#### 2. TimingInterceptor - 性能监控

监控工具执行时间：

```java
public class TimingInterceptor implements ToolInterceptor {
    @Override
    public void after(ToolInvocation invocation, Object result) {
        System.out.println("[TIMING] Tool executed in " + 
            invocation.getElapsedTime() + "ms");
    }
}
```

#### 3. ValidationInterceptor - 参数验证

验证工具参数：

```java
public class ValidationInterceptor implements ToolInterceptor {
    @Override
    public void before(ToolInvocation invocation) throws Exception {
        for (Object arg : invocation.getArgs()) {
            if (arg == null) {
                throw new IllegalArgumentException("Argument cannot be null");
            }
        }
    }
}
```

### 使用方式

#### 方式 1: 在 @Tool 注解中配置

```java
@Tool(
    description = "Get weather information",
    interceptors = {LoggingInterceptor.class, TimingInterceptor.class}
)
public String getWeather(WeatherRequest request) {
    // 工具实现
}
```

#### 方式 2: 添加全局拦截器

```java
ToolExecutor executor = new ToolExecutor();

// 添加全局拦截器（对所有工具生效）
executor.addGlobalInterceptor(new TimingInterceptor());
executor.addGlobalInterceptor(new LoggingInterceptor());

executor.registerTool(new MyTools());
```

### 拦截器执行顺序

```
执行前 (before): 全局拦截器 -> 方法拦截器
执行后 (after):  方法拦截器 -> 全局拦截器 (反向)
异常处理 (onError): 方法拦截器 -> 全局拦截器 (反向)
```

### 自定义拦截器示例

#### 权限检查拦截器

```java
public class AuthorizationInterceptor implements ToolInterceptor {
    @Override
    public void before(ToolInvocation invocation) throws Exception {
        String userId = (String) invocation.getAttribute("userId");
        if (userId == null) {
            throw new SecurityException("User not authenticated");
        }
        
        // 检查权限
        if (!hasPermission(userId, invocation.getToolName())) {
            throw new SecurityException("User not authorized");
        }
    }
    
    private boolean hasPermission(String userId, String toolName) {
        // 实现权限检查逻辑
        return true;
    }
}
```

#### 缓存拦截器

```java
public class CachingInterceptor implements ToolInterceptor {
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    
    @Override
    public void before(ToolInvocation invocation) {
        String cacheKey = buildCacheKey(invocation);
        if (cache.containsKey(cacheKey)) {
            invocation.setAttribute("cached", cache.get(cacheKey));
        }
    }
    
    @Override
    public void after(ToolInvocation invocation, Object result) {
        if (!invocation.getContext().containsKey("cached")) {
            String cacheKey = buildCacheKey(invocation);
            cache.put(cacheKey, result);
        }
    }
}
```

#### 重试拦截器

```java
public class RetryInterceptor implements ToolInterceptor {
    private final int maxRetries = 3;
    
    @Override
    public void onError(ToolInvocation invocation, Exception error) throws Exception {
        Integer retryCount = (Integer) invocation.getAttribute("retryCount");
        if (retryCount == null) {
            retryCount = 0;
        }
        
        if (retryCount < maxRetries) {
            invocation.setAttribute("retryCount", retryCount + 1);
            System.out.println("Retrying... Attempt: " + (retryCount + 1));
            // 重试逻辑
        } else {
            throw error;
        }
    }
}
```

---

## 二、LLM Message 领域模型

### 概述

完整的消息模型，支持构建 LLM 对话上下文，兼容主流 LLM 平台格式。

### 核心类

#### 1. MessageRole - 消息角色

```java
public enum MessageRole {
    SYSTEM,      // 系统消息
    USER,        // 用户消息
    ASSISTANT,   // 助手消息
    TOOL,        // 工具结果消息
    FUNCTION     // 函数消息（兼容）
}
```

#### 2. Message - 消息基类

```java
public abstract class Message {
    MessageRole getRole()
    String getContent()
    void setContent(String content)
    Map<String, Object> getMetadata()
    void addMetadata(String key, Object value)
    JsonObject toOpenAIFormat()
    JsonObject toAnthropicFormat()
}
```

#### 3. SystemMessage - 系统消息

```java
SystemMessage message = new SystemMessage("You are a helpful assistant.");
// 或使用工厂方法
SystemMessage message = SystemMessage.of("You are a helpful assistant.");
```

#### 4. UserMessage - 用户消息

```java
UserMessage message = new UserMessage("What's the weather in Paris?");
// 或
UserMessage message = UserMessage.of("What's the weather in Paris?");
```

#### 5. AssistantMessage - 助手消息

```java
// 普通回复
AssistantMessage message = AssistantMessage.of("The weather is sunny.");

// 带工具调用
AssistantMessage message = new AssistantMessage();
message.addToolCall("call_123", "getWeather", "{\"city\":\"Paris\"}");
```

#### 6. ToolMessage - 工具结果消息

```java
ToolMessage message = ToolMessage.of(
    "call_123",              // tool_call_id
    "Weather: Sunny, 20°C"   // 结果
);
```

#### 7. ToolCall - 工具调用

```java
ToolCall toolCall = new ToolCall(
    "call_123",                    // id
    "getWeather",                  // 工具名称
    "{\"city\":\"Paris\"}"         // 参数 JSON
);
```

#### 8. Conversation - 对话管理

```java
Conversation conversation = new Conversation();

// 添加消息
conversation.addSystem("You are helpful");
conversation.addUser("Hello");
conversation.addAssistant("Hi there!");

// 获取消息列表
List<Message> messages = conversation.getMessages();

// 获取最后一条消息
Message lastMessage = conversation.getLastMessage();

// 转换为 LLM 格式
JsonArray openAIFormat = conversation.toOpenAIFormat();
JsonArray anthropicFormat = conversation.toAnthropicFormat();
```

#### 9. MessageBuilder - 消息构建器

```java
// 便捷的消息构建
Message system = MessageBuilder.system("System prompt");
Message user = MessageBuilder.user("User input");
Message assistant = MessageBuilder.assistant("Assistant response");
Message tool = MessageBuilder.tool("call_id", "Tool result");
```

### 使用示例

#### 示例 1: 基本对话

```java
Conversation conversation = new Conversation();
conversation.addSystem("You are a helpful AI assistant.");
conversation.addUser("What is 2+2?");
conversation.addAssistant("2+2 equals 4.");

// 发送给 OpenAI
JsonArray messages = conversation.toOpenAIFormat();
// chatRequest.add("messages", messages);
```

#### 示例 2: Tool Calling 流程

```java
Conversation conversation = new Conversation();

// 1. 用户提问
conversation.addUser("What's the weather in Beijing?");

// 2. LLM 决定调用工具
AssistantMessage assistantMsg = new AssistantMessage();
assistantMsg.addToolCall("call_abc", "getWeather", "{\"city\":\"Beijing\"}");
conversation.addMessage(assistantMsg);

// 3. 执行工具并返回结果
String toolResult = "Weather in Beijing: Sunny, 25°C";
conversation.addTool("call_abc", toolResult);

// 4. LLM 最终回复
conversation.addAssistant("The weather in Beijing is sunny with 25°C.");

// 5. 转换为 OpenAI 格式发送
JsonArray messages = conversation.toOpenAIFormat();
```

#### 示例 3: 多个工具调用

```java
AssistantMessage message = new AssistantMessage();
message.addToolCall("call_1", "getWeather", "{\"city\":\"Paris\"}");
message.addToolCall("call_2", "getWeather", "{\"city\":\"Tokyo\"}");
message.addToolCall("call_3", "getTime", "{}");

conversation.addMessage(message);

// 返回每个工具的结果
conversation.addTool("call_1", "Paris: Sunny, 20°C");
conversation.addTool("call_2", "Tokyo: Cloudy, 18°C");
conversation.addTool("call_3", "Current time: 14:30");
```

#### 示例 4: 消息元数据

```java
UserMessage message = MessageBuilder.user("Calculate something");
message.addMetadata("timestamp", System.currentTimeMillis());
message.addMetadata("userId", "user_123");
message.addMetadata("sessionId", "session_456");

// 后续可以获取元数据
String userId = (String) message.getMetadata("userId");
```

#### 示例 5: OpenAI 格式

```java
Conversation conversation = new Conversation()
    .addSystem("You are helpful")
    .addUser("Hello");

JsonArray openAIMessages = conversation.toOpenAIFormat();
// 输出:
// [
//   {"role": "system", "content": "You are helpful"},
//   {"role": "user", "content": "Hello"}
// ]
```

#### 示例 6: Anthropic Claude 格式

```java
Conversation conversation = new Conversation()
    .addUser("What's the weather?");

AssistantMessage assistantMsg = new AssistantMessage();
assistantMsg.addToolCall("call_1", "getWeather", "{\"city\":\"London\"}");
conversation.addMessage(assistantMsg);

JsonArray anthropicMessages = conversation.toAnthropicFormat();
// Anthropic 格式略有不同，tool_use 作为 content 的一部分
```

### 格式对比

| 特性 | OpenAI | Anthropic |
|------|--------|-----------|
| System 消息 | ✅ role="system" | ✅ role="system" |
| Tool Calls | ✅ tool_calls 数组 | ✅ content 中的 tool_use |
| Tool Results | ✅ role="tool" | ✅ role="user" + tool_result |
| 多 Tool Calls | ✅ 数组 | ✅ content 数组 |

## 完整示例

### Tool 拦截器 + Message 集成

```java
public class CompleteExample {
    public static void main(String[] args) throws Exception {
        // 1. 创建 ToolExecutor 并配置拦截器
        ToolExecutor executor = new ToolExecutor();
        executor.addGlobalInterceptor(new TimingInterceptor());
        executor.addGlobalInterceptor(new LoggingInterceptor());
        
        // 2. 注册工具
        executor.registerTool(new WeatherTools());
        
        // 3. 构建对话
        Conversation conversation = new Conversation();
        conversation.addSystem("You are a weather assistant.");
        conversation.addUser("What's the weather in Tokyo?");
        
        // 4. 模拟 LLM 返回（决定调用工具）
        AssistantMessage llmResponse = new AssistantMessage();
        llmResponse.addToolCall("call_123", "getWeather", 
            "{\"city\":\"Tokyo\",\"unit\":\"celsius\"}");
        conversation.addMessage(llmResponse);
        
        // 5. 执行工具（带拦截器）
        String toolResult = (String) executor.executeTool("getWeather", 
            "{\"city\":\"Tokyo\",\"unit\":\"celsius\"}");
        
        // 6. 添加工具结果到对话
        conversation.addTool("call_123", toolResult);
        
        // 7. 添加最终回复
        conversation.addAssistant("The weather in Tokyo is " + toolResult);
        
        // 8. 输出完整对话
        System.out.println(conversation);
        
        // 9. 转换为 OpenAI 格式
        JsonArray messages = conversation.toOpenAIFormat();
        System.out.println(new GsonBuilder().setPrettyPrinting()
            .create().toJson(messages));
    }
}
```

## 测试结果

✅ **37 个测试全部通过**
- JsonSchemaGeneratorTest: 14 个测试
- ToolExecutorTest: 13 个测试  
- MessageTest: 10 个测试 ⭐ NEW

## 项目结构更新

```
src/main/java/com/jsonschema/
├── annotations/
│   ├── SchemaDescription.java
│   ├── SchemaProperty.java
│   ├── SchemaIgnore.java
│   └── Tool.java (更新: 添加 interceptors 属性)
├── tool/
│   ├── ToolDefinition.java (更新: 添加拦截器支持)
│   ├── ToolExecutor.java (更新: 实现拦截器调用)
│   └── interceptor/ ⭐ NEW
│       ├── ToolInterceptor.java
│       ├── ToolInvocation.java
│       ├── LoggingInterceptor.java
│       ├── TimingInterceptor.java
│       └── ValidationInterceptor.java
└── llm/ ⭐ NEW
    └── message/
        ├── MessageRole.java
        ├── Message.java
        ├── SystemMessage.java
        ├── UserMessage.java
        ├── AssistantMessage.java
        ├── ToolMessage.java
        ├── ToolCall.java
        ├── Conversation.java
        └── MessageBuilder.java
```

## 运行示例

```bash
# 拦截器示例
mvn test-compile
java -cp "target/test-classes:target/classes:..." \
    com.jsonschema.tool.InterceptorExample

# Message 示例
java -cp "target/test-classes:target/classes:..." \
    com.jsonschema.llm.message.MessageExample

# 运行测试
mvn test
```

## 总结

### 新增功能

1. ✅ **Tool 拦截器系统**
   - 前置、后置和错误处理钩子
   - 全局和方法级别配置
   - 内置常用拦截器
   - 支持自定义拦截器

2. ✅ **LLM Message 模型**
   - 完整的消息类型支持
   - OpenAI 和 Anthropic 格式转换
   - 对话管理
   - Builder 模式支持
   - 元数据支持

### 优势

- **可扩展性**: 轻松添加自定义拦截器和消息类型
- **标准化**: 统一的消息模型，支持多个 LLM 平台
- **易用性**: 简洁的 API 和 Builder 模式
- **测试完整**: 37 个单元测试覆盖核心功能

现在你的 LLM 工具系统更加完整和强大！🎉
