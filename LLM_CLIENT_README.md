# LLM 客户端 - 使用指南

## 概述

基于 OkHttp 的 LLM 客户端，支持 OpenAI 和 Anthropic API，提供统一的接口进行大模型调用。

## 核心特性

✅ **统一接口**: 支持 OpenAI 和 Anthropic API  
✅ **类型安全**: 强类型的请求和响应模型  
✅ **消息管理**: 与 Message 模型无缝集成  
✅ **Tool Calling**: 完整的工具调用支持  
✅ **Builder 模式**: 便捷的请求构建  
✅ **可配置**: 支持自定义超时、BaseURL 等  

---

## 快速开始

### 1. OpenAI 客户端

```java
// 创建客户端
OpenAIClient client = OpenAIClient.builder()
    .apiKey("your-api-key")
    .build();

// 构建请求
ChatRequest request = ChatRequestBuilder.create("gpt-3.5-turbo")
    .message(MessageBuilder.system("You are a helpful assistant."))
    .message(MessageBuilder.user("What is 2+2?"))
    .temperature(0.7)
    .maxTokens(100)
    .build();

// 发送请求
try {
    ChatResponse response = client.chat(request);
    System.out.println(response.getAssistantMessage().getContent());
    System.out.println("Usage: " + response.getUsage());
} catch (IOException e) {
    e.printStackTrace();
} finally {
    client.close();
}
```

### 2. Anthropic 客户端

```java
// 创建客户端
AnthropicClient client = AnthropicClient.builder()
    .apiKey("your-api-key")
    .build();

// 构建请求
ChatRequest request = ChatRequestBuilder.create("claude-3-opus-20240229")
    .message(MessageBuilder.user("Tell me a joke"))
    .maxTokens(1024)
    .temperature(0.7)
    .build();

// 发送请求
try {
    ChatResponse response = client.chat(request);
    System.out.println(response.getAssistantMessage().getContent());
} catch (IOException e) {
    e.printStackTrace();
} finally {
    client.close();
}
```

---

## 核心组件

### 1. ChatRequest - 请求体

```java
public class ChatRequest {
    String model;              // 模型名称
    List<Message> messages;    // 消息列表
    List<JsonObject> tools;    // 工具列表
    Double temperature;        // 温度 (0-2)
    Integer maxTokens;         // 最大 token 数
    Double topP;               // Top-P 采样
    Integer n;                 // 生成数量
    Boolean stream;            // 是否流式
    List<String> stop;         // 停止词
}
```

#### 方法

- `toOpenAIJson()`: 转换为 OpenAI 格式
- `toAnthropicJson()`: 转换为 Anthropic 格式

### 2. ChatResponse - 响应体

```java
public class ChatResponse {
    String id;                 // 响应 ID
    String model;              // 使用的模型
    List<Choice> choices;      // 响应选项
    Usage usage;               // Token 使用统计
}
```

#### 方法

- `getFirstChoice()`: 获取第一个选项
- `getAssistantMessage()`: 获取助手消息
- `fromOpenAIJson(JsonObject)`: 从 OpenAI 响应解析
- `fromAnthropicJson(JsonObject)`: 从 Anthropic 响应解析

### 3. ChatRequestBuilder - 请求构建器

```java
ChatRequestBuilder builder = ChatRequestBuilder.create("gpt-4")
    .message(MessageBuilder.system("System prompt"))
    .message(MessageBuilder.user("User input"))
    .conversation(conversation)        // 从 Conversation 构建
    .tools(toolExecutor)               // 从 ToolExecutor 添加工具
    .temperature(0.7)
    .maxTokens(500)
    .topP(0.9)
    .stop("END")
    .build();
```

### 4. LLM 客户端

#### OpenAIClient

```java
OpenAIClient client = OpenAIClient.builder()
    .apiKey("your-api-key")
    .baseUrl("https://api.openai.com/v1")  // 可选，默认官方 URL
    .connectTimeout(30)                     // 连接超时（秒）
    .readTimeout(60)                        // 读取超时（秒）
    .writeTimeout(30)                       // 写入超时（秒）
    .build();
```

#### AnthropicClient

```java
AnthropicClient client = AnthropicClient.builder()
    .apiKey("your-api-key")
    .baseUrl("https://api.anthropic.com/v1")  // 可选
    .version("2023-06-01")                     // API 版本
    .connectTimeout(30)
    .readTimeout(60)
    .writeTimeout(30)
    .build();
```

---

## 使用示例

### 示例 1: 简单对话

```java
OpenAIClient client = OpenAIClient.builder()
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .build();

try {
    ChatRequest request = ChatRequestBuilder.create("gpt-3.5-turbo")
        .message(MessageBuilder.user("What is the capital of France?"))
        .temperature(0.5)
        .maxTokens(50)
        .build();
    
    ChatResponse response = client.chat(request);
    System.out.println("Answer: " + response.getAssistantMessage().getContent());
    System.out.println("Tokens used: " + response.getUsage().getTotalTokens());
    
} catch (IOException e) {
    System.err.println("API call failed: " + e.getMessage());
} finally {
    client.close();
}
```

### 示例 2: 多轮对话

```java
Conversation conversation = new Conversation();
conversation.addSystem("You are a helpful math tutor.");
conversation.addUser("What is 15 + 27?");
conversation.addAssistant("15 + 27 equals 42.");
conversation.addUser("And what is that multiplied by 2?");

ChatRequest request = ChatRequestBuilder.create("gpt-3.5-turbo")
    .conversation(conversation)
    .temperature(0.3)
    .build();

ChatResponse response = client.chat(request);
System.out.println(response.getAssistantMessage().getContent());
// Output: "42 multiplied by 2 equals 84."
```

### 示例 3: 使用 Tools

```java
// 1. 注册工具
ToolExecutor toolExecutor = new ToolExecutor();
toolExecutor.registerTool(new WeatherTools());

// 2. 构建请求
Conversation conversation = new Conversation();
conversation.addUser("What's the weather in Tokyo?");

ChatRequest request = ChatRequestBuilder.create("gpt-4")
    .conversation(conversation)
    .tools(toolExecutor)  // 自动添加所有工具的 Schema
    .build();

// 3. 发送请求
ChatResponse response = client.chat(request);

// 4. 检查是否有工具调用
AssistantMessage assistantMsg = response.getAssistantMessage();
if (assistantMsg.hasToolCalls()) {
    conversation.addMessage(assistantMsg);
    
    // 5. 执行工具
    for (ToolCall toolCall : assistantMsg.getToolCalls()) {
        Object result = toolExecutor.executeTool(
            toolCall.getName(),
            toolCall.getArguments()
        );
        conversation.addTool(toolCall.getId(), result.toString());
    }
    
    // 6. 发送工具结果
    ChatRequest followUpRequest = ChatRequestBuilder.create("gpt-4")
        .conversation(conversation)
        .tools(toolExecutor)
        .build();
    
    ChatResponse finalResponse = client.chat(followUpRequest);
    System.out.println(finalResponse.getAssistantMessage().getContent());
}
```

### 示例 4: 完整的 Tool Calling 流程

```java
public class WeatherAssistant {
    
    public static void main(String[] args) throws Exception {
        OpenAIClient client = OpenAIClient.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .build();
        
        ToolExecutor toolExecutor = new ToolExecutor();
        toolExecutor.registerTool(new WeatherTools());
        
        try {
            String userInput = "What's the weather in Paris and London?";
            Conversation conversation = new Conversation();
            conversation.addUser(userInput);
            
            while (true) {
                ChatRequest request = ChatRequestBuilder.create("gpt-4")
                    .conversation(conversation)
                    .tools(toolExecutor)
                    .build();
                
                ChatResponse response = client.chat(request);
                AssistantMessage assistantMsg = response.getAssistantMessage();
                
                if (assistantMsg.hasToolCalls()) {
                    conversation.addMessage(assistantMsg);
                    
                    for (ToolCall toolCall : assistantMsg.getToolCalls()) {
                        System.out.println("Calling: " + toolCall.getName());
                        
                        Object result = toolExecutor.executeTool(
                            toolCall.getName(),
                            toolCall.getArguments()
                        );
                        
                        conversation.addTool(toolCall.getId(), result.toString());
                    }
                } else {
                    System.out.println("Assistant: " + assistantMsg.getContent());
                    break;
                }
            }
        } finally {
            client.close();
        }
    }
}
```

### 示例 5: Anthropic Claude 使用

```java
AnthropicClient client = AnthropicClient.builder()
    .apiKey(System.getenv("ANTHROPIC_API_KEY"))
    .build();

try {
    ChatRequest request = ChatRequestBuilder.create("claude-3-opus-20240229")
        .message(MessageBuilder.user("Write a haiku about coding"))
        .maxTokens(1024)
        .temperature(0.8)
        .build();
    
    ChatResponse response = client.chat(request);
    System.out.println(response.getAssistantMessage().getContent());
    
} finally {
    client.close();
}
```

---

## 高级配置

### 自定义 HTTP 客户端

```java
OkHttpClient customHttpClient = new OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(120, TimeUnit.SECONDS)
    .addInterceptor(chain -> {
        // 自定义拦截器
        Request request = chain.request().newBuilder()
            .addHeader("Custom-Header", "value")
            .build();
        return chain.proceed(request);
    })
    .build();

OpenAIClient client = new OpenAIClient(
    "your-api-key",
    "https://api.openai.com/v1",
    customHttpClient
);
```

### 使用代理

```java
Proxy proxy = new Proxy(
    Proxy.Type.HTTP,
    new InetSocketAddress("proxy.example.com", 8080)
);

OkHttpClient httpClient = new OkHttpClient.Builder()
    .proxy(proxy)
    .build();

OpenAIClient client = OpenAIClient.builder()
    .apiKey("your-api-key")
    .httpClient(httpClient)
    .build();
```

### 自定义 Base URL (兼容其他服务)

```java
// 使用兼容 OpenAI API 的服务
OpenAIClient client = OpenAIClient.builder()
    .apiKey("your-api-key")
    .baseUrl("https://your-custom-endpoint.com/v1")
    .build();
```

---

## 错误处理

```java
try {
    ChatResponse response = client.chat(request);
    
    if (response.getChoices().isEmpty()) {
        System.err.println("No response choices returned");
        return;
    }
    
    AssistantMessage message = response.getAssistantMessage();
    System.out.println(message.getContent());
    
} catch (IOException e) {
    // 网络错误或 API 错误
    System.err.println("API call failed: " + e.getMessage());
    
    if (e.getMessage().contains("401")) {
        System.err.println("Authentication failed. Check your API key.");
    } else if (e.getMessage().contains("429")) {
        System.err.println("Rate limit exceeded. Please retry later.");
    } else if (e.getMessage().contains("500")) {
        System.err.println("Server error. Please retry.");
    }
}
```

---

## 最佳实践

### 1. 资源管理

```java
OpenAIClient client = OpenAIClient.builder()
    .apiKey("your-api-key")
    .build();

try {
    // 使用客户端
} finally {
    client.close();  // 确保关闭客户端释放资源
}
```

### 2. 重用客户端

```java
// 不要为每个请求创建新客户端
// 好的做法：创建一个客户端实例并重用
public class LLMService {
    private final OpenAIClient client;
    
    public LLMService(String apiKey) {
        this.client = OpenAIClient.builder()
            .apiKey(apiKey)
            .build();
    }
    
    public String chat(String userMessage) throws IOException {
        ChatRequest request = ChatRequestBuilder.create("gpt-3.5-turbo")
            .message(MessageBuilder.user(userMessage))
            .build();
        
        ChatResponse response = client.chat(request);
        return response.getAssistantMessage().getContent();
    }
    
    public void shutdown() {
        client.close();
    }
}
```

### 3. 处理 Token 限制

```java
ChatRequest request = ChatRequestBuilder.create("gpt-3.5-turbo")
    .message(MessageBuilder.user(longText))
    .maxTokens(4096)  // 设置合理的上限
    .build();

ChatResponse response = client.chat(request);

// 检查是否因 token 限制而截断
if ("length".equals(response.getFirstChoice().getFinishReason())) {
    System.out.println("Response was truncated due to token limit");
}
```

### 4. 温度和采样参数

```java
// 确定性输出（如代码生成、数据提取）
ChatRequest deterministicRequest = ChatRequestBuilder.create("gpt-4")
    .message(MessageBuilder.user("Extract the email from: ..."))
    .temperature(0.0)
    .build();

// 创造性输出（如故事生成、头脑风暴）
ChatRequest creativeRequest = ChatRequestBuilder.create("gpt-4")
    .message(MessageBuilder.user("Write a creative story about..."))
    .temperature(0.8)
    .topP(0.95)
    .build();
```

---

## 与现有组件集成

### 与 Message 模型集成

```java
// 从 Conversation 构建请求
Conversation conversation = new Conversation();
conversation.addSystem("You are helpful");
conversation.addUser("Hello");

ChatRequest request = ChatRequestBuilder.create("gpt-3.5-turbo")
    .conversation(conversation)
    .build();
```

### 与 ToolExecutor 集成

```java
// 自动添加所有注册的工具
ToolExecutor toolExecutor = new ToolExecutor();
toolExecutor.registerTool(new MyTools());

ChatRequest request = ChatRequestBuilder.create("gpt-4")
    .tools(toolExecutor)  // 自动获取所有工具的 Schema
    .build();
```

### 与拦截器集成

```java
ToolExecutor toolExecutor = new ToolExecutor();
toolExecutor.addGlobalInterceptor(new LoggingInterceptor());
toolExecutor.registerTool(new MyTools());

// Tool 执行时会自动记录日志
Object result = toolExecutor.executeTool(toolName, arguments);
```

---

## API 兼容性

### OpenAI 兼容的端点

客户端支持任何兼容 OpenAI API 的服务：

- OpenAI 官方
- Azure OpenAI
- 第三方兼容服务

```java
OpenAIClient client = OpenAIClient.builder()
    .apiKey("your-key")
    .baseUrl("https://your-service.com/v1")
    .build();
```

### Anthropic Claude

完全支持 Anthropic Claude API，包括：

- Claude 3 Opus
- Claude 3 Sonnet
- Claude 3 Haiku
- Tool Use (Function Calling)

---

## 环境变量配置

建议使用环境变量存储 API Key：

```bash
# Linux/Mac
export OPENAI_API_KEY="sk-..."
export ANTHROPIC_API_KEY="sk-ant-..."

# Windows
set OPENAI_API_KEY=sk-...
set ANTHROPIC_API_KEY=sk-ant-...
```

在代码中读取：

```java
String apiKey = System.getenv("OPENAI_API_KEY");
if (apiKey == null || apiKey.isEmpty()) {
    throw new IllegalStateException("OPENAI_API_KEY not set");
}

OpenAIClient client = OpenAIClient.builder()
    .apiKey(apiKey)
    .build();
```

---

## 总结

### 核心优势

1. **统一接口**: 一套代码支持多个 LLM 平台
2. **类型安全**: 编译时检查，减少运行时错误
3. **无缝集成**: 与消息模型和工具执行器完美配合
4. **易于使用**: Builder 模式，链式调用
5. **生产就绪**: 完整的错误处理和资源管理

### 适用场景

- ✅ 聊天机器人
- ✅ AI 助手
- ✅ 代码生成
- ✅ 内容创作
- ✅ 数据分析
- ✅ 自动化任务

现在你可以轻松地在 Java 应用中集成大模型能力！🚀
