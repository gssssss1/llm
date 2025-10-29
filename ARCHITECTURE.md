# Java LLM Tool 系统 - 架构文档

## 系统概览

本项目是一个完整的 Java LLM 工具系统，提供从 JSON Schema 生成到对话管理的全栈解决方案。

## 架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                         应用层                                   │
├─────────────────────────────────────────────────────────────────┤
│  SessionManager  │  Session  │  EventHandler  │  Memory         │
├─────────────────────────────────────────────────────────────────┤
│                      会话管理层                                  │
├─────────────────────────────────────────────────────────────────┤
│  Event System  │  Context Manager  │  Memory System             │
├─────────────────────────────────────────────────────────────────┤
│                      工具执行层                                  │
├─────────────────────────────────────────────────────────────────┤
│  ToolExecutor  │  Tool Interceptors  │  ToolDefinition          │
├─────────────────────────────────────────────────────────────────┤
│                      消息处理层                                  │
├─────────────────────────────────────────────────────────────────┤
│  Message Models  │  Conversation  │  MessageBuilder             │
├─────────────────────────────────────────────────────────────────┤
│                      客户端层                                    │
├─────────────────────────────────────────────────────────────────┤
│  OpenAIClient  │  AnthropicClient  │  ChatRequest/Response      │
├─────────────────────────────────────────────────────────────────┤
│                      Schema 生成层                               │
├─────────────────────────────────────────────────────────────────┤
│  JsonSchemaGenerator  │  JsonSchema  │  Annotations             │
└─────────────────────────────────────────────────────────────────┘
```

## 核心模块

### 1. Schema 生成模块

**职责**: 将 Java POJO 转换为 JSON Schema

**核心类**:
- `JsonSchemaGenerator`: Schema 生成器
- `JsonSchema`: Schema 模型
- `@SchemaDescription`, `@SchemaProperty`: 注解

**使用场景**:
- 为 LLM Function Calling 生成参数 Schema
- API 文档生成
- 数据验证

### 2. HTTP 客户端模块

**职责**: 与 LLM API 通信

**核心类**:
- `LLMClient`: 客户端接口
- `OpenAIClient`: OpenAI 实现
- `AnthropicClient`: Anthropic 实现
- `ChatRequest/ChatResponse`: 请求响应模型

**特性**:
- 统一接口设计
- 自动格式转换
- 超时和重试配置

### 3. 消息处理模块

**职责**: 管理 LLM 消息和对话

**核心类**:
- `Message`: 消息基类
- `UserMessage`, `AssistantMessage`, `ToolMessage`: 具体消息类型
- `Conversation`: 对话管理
- `ToolCall`: 工具调用模型

**特性**:
- OpenAI/Anthropic 格式转换
- Builder 模式
- 元数据支持

### 4. 工具执行模块

**职责**: 动态执行 LLM 工具

**核心类**:
- `ToolExecutor`: 工具执行器
- `ToolDefinition`: 工具定义
- `@Tool`: 工具注解
- `ToolInterceptor`: 拦截器接口

**特性**:
- 注解驱动
- 反射执行
- 拦截器链
- 自动 Schema 生成

### 5. 事件系统模块

**职责**: 记录和处理会话事件

**核心类**:
- `Event`: 事件基类
- `EventType`: 事件类型枚举
- `UserMessageEvent`, `AssistantMessageEvent`, `ToolCallEvent`, etc.: 具体事件
- `EventHandler`: 事件处理器接口

**特性**:
- 事件驱动架构
- 可观察性
- 审计跟踪

### 6. 记忆系统模块

**职责**: 管理对话记忆

**核心类**:
- `Memory`: 记忆接口
- `ShortTermMemory`: 短期记忆实现

**特性**:
- 可扩展接口
- 大小限制
- 键值存储

### 7. 上下文管理模块

**职责**: 管理对话上下文

**核心类**:
- `ContextManager`: 上下文管理器
- `MessageFilter`: 消息过滤器
- `TokenCounter`: Token 计数器

**特性**:
- Token 限制
- 自动压缩
- 查询和删除
- 过滤器组合

### 8. 会话管理模块

**职责**: 管理用户会话

**核心类**:
- `Session`: 会话类
- `SessionManager`: 会话管理器

**特性**:
- 完整的会话生命周期
- 事件集成
- 自动工具调用
- 多用户支持
- 超时管理

## 数据流

### 简单对话流程

```
User Input
    ↓
Session.sendMessage()
    ↓
[Event] UserMessageEvent
    ↓
Add to: Conversation, Context, Memory
    ↓
Context.compress() (if needed)
    ↓
ChatRequestBuilder
    ↓
LLMClient.chat()
    ↓
[HTTP] OpenAI/Anthropic API
    ↓
ChatResponse.parse()
    ↓
[Event] AssistantMessageEvent
    ↓
Add to: Conversation, Context, Memory
    ↓
Return Response
```

### 工具调用流程

```
User Input: "What's the weather?"
    ↓
Session.sendMessage()
    ↓
[Event] UserMessageEvent
    ↓
LLM decides to call tool
    ↓
[Event] ToolCallEvent
    ↓
ToolExecutor.executeTool()
    ↓
[Interceptor] before()
    ↓
[Reflection] method.invoke()
    ↓
[Interceptor] after()
    ↓
[Event] ToolResultEvent
    ↓
Add ToolMessage to Context
    ↓
LLM Follow-up Call
    ↓
[Event] AssistantMessageEvent
    ↓
Return Final Response
```

## 设计模式

### 1. Builder Pattern
用于构建复杂对象：
- `ChatRequestBuilder`
- `Session.Builder`
- `MessageBuilder`

### 2. Strategy Pattern
用于算法替换：
- `TokenCounter` 接口
- `Memory` 接口
- `LLMClient` 接口

### 3. Observer Pattern
用于事件处理：
- `EventHandler` 接口
- `Session.addEventListener()`

### 4. Chain of Responsibility
用于拦截器链：
- `ToolInterceptor` 链式执行

### 5. Factory Pattern
用于消息创建：
- `MessageBuilder` 工厂方法

### 6. Template Method
用于事件处理：
- `EventHandler` 默认方法

## 扩展点

### 1. 自定义记忆实现

```java
public class RedisMemory implements Memory {
    private final RedisClient redis;
    
    @Override
    public void add(Message message) {
        redis.lpush(sessionId, serialize(message));
    }
    
    // ... 其他方法
}
```

### 2. 自定义 Token 计数器

```java
public class OpenAITokenCounter implements TokenCounter {
    @Override
    public int count(String text) {
        // 使用 tiktoken 库
        return tiktoken.encode(text).size();
    }
}
```

### 3. 自定义事件处理器

```java
public class DatabaseEventLogger implements EventHandler {
    @Override
    public void onEvent(Event event) {
        database.insert(event);
    }
}
```

### 4. 自定义拦截器

```java
public class RateLimitInterceptor implements ToolInterceptor {
    @Override
    public void before(ToolInvocation invocation) {
        if (!rateLimiter.tryAcquire()) {
            throw new RateLimitException();
        }
    }
}
```

## 性能考虑

### 1. 内存管理
- ShortTermMemory 有大小限制
- ContextManager 自动压缩
- SessionManager 自动清理过期会话

### 2. 并发控制
- ConcurrentHashMap 用于 SessionManager
- Synchronized collections 用于 ShortTermMemory
- 无状态的 LLMClient 可共享

### 3. 资源释放
- LLMClient 需要显式 close()
- Session 可以 close() 标记为非活跃
- SessionManager 定期清理

## 安全考虑

### 1. API Key 管理
- 使用环境变量
- 不要硬编码
- 考虑使用密钥管理服务

### 2. 输入验证
- ValidationInterceptor
- 参数验证
- Schema 验证

### 3. 日志敏感信息
- 避免记录完整消息内容
- 脱敏处理
- 审计日志分离

## 监控和观察

### 1. 事件监控
```java
session.addEventListener(new MetricsEventHandler() {
    @Override
    public void onAssistantMessage(AssistantMessageEvent event) {
        metrics.record("tokens_used", event.getTokensUsed());
        metrics.record("response_time", event.getElapsedTime());
    }
});
```

### 2. 性能监控
```java
session.addEventListener(new PerformanceEventHandler() {
    @Override
    public void onToolCall(ToolCallEvent event) {
        timer.start(event.getToolName());
    }
    
    @Override
    public void onToolResult(ToolResultEvent event) {
        timer.stop(event.getToolName());
    }
});
```

## 测试策略

### 1. 单元测试
- 每个模块独立测试
- Mock LLMClient 进行测试
- 测试边界条件

### 2. 集成测试
- 测试完整流程
- 使用测试 API key
- 测试工具调用链

### 3. 性能测试
- 上下文压缩性能
- 大量会话管理
- 并发请求处理

## 最佳实践

### 1. Session 使用
- 一个用户一个会话
- 定期保存会话状态
- 合理设置超时时间

### 2. 上下文管理
- 根据模型设置合适的 token 限制
- 定期压缩上下文
- 删除无关消息

### 3. 事件处理
- 避免阻塞事件处理器
- 考虑异步处理
- 错误隔离

### 4. 工具执行
- 设置合理的超时
- 错误处理
- 日志记录

## 总结

本系统提供了一个完整的、生产就绪的 LLM 工具平台，具有：

✅ 模块化设计  
✅ 可扩展架构  
✅ 事件驱动  
✅ 生产级特性  
✅ 完整的文档  

适用于构建各类 AI 对话应用。
