# Advanced Features Guide

本文档介绍Session-Based LLM Framework的高级特性和功能。

## Table of Contents

1. [高级拦截器系统](#高级拦截器系统)
2. [增强的工具系统](#增强的工具系统)
3. [会话池化](#会话池化)
4. [监控和度量](#监控和度量)
5. [提示模板引擎](#提示模板引擎)
6. [安全特性](#安全特性)

---

## 高级拦截器系统

框架提供了全面的拦截器系统，支持以下功能：

### 1. CachingInterceptor - 缓存拦截器

自动缓存响应以减少API调用：

```java
CachingInterceptor caching = new CachingInterceptor(
    Duration.ofMinutes(15),  // TTL
    1000                     // 最大缓存条目数
);
```

**特性：**
- 基于请求内容的SHA-256哈希缓存
- 可配置的TTL (Time-To-Live)
- LRU驱逐策略
- 缓存大小限制

### 2. RetryInterceptor - 重试拦截器

实现指数退避和抖动的智能重试：

```java
RetryInterceptor retry = new RetryInterceptor(
    3,                           // 最大重试次数
    Duration.ofMillis(500),      // 初始退避时间
    Duration.ofSeconds(5)        // 最大退避时间
);
```

**特性：**
- 指数退避算法
- 随机抖动以避免雷击效应
- 特殊处理RateLimitException
- 可配置的最大重试次数和退避时间

### 3. RateLimitInterceptor - 限流拦截器

基于滑动窗口算法的限流：

```java
RateLimitInterceptor rateLimiter = new RateLimitInterceptor(
    10,                          // 窗口内最大请求数
    Duration.ofMinutes(1)        // 窗口时长
);
```

**特性：**
- 滑动窗口算法
- 精确的请求计数
- 线程安全
- 实时限流状态查询

### 4. CircuitBreakerInterceptor - 熔断器拦截器

防止级联故障的熔断器模式实现：

```java
CircuitBreakerInterceptor circuitBreaker = new CircuitBreakerInterceptor(
    5,                           // 失败阈值
    Duration.ofSeconds(30)       // 熔断超时时间
);
```

**状态机：**
- **CLOSED**: 正常运行
- **OPEN**: 熔断打开，拒绝请求
- **HALF_OPEN**: 半开状态，尝试恢复

### 5. ContentFilterInterceptor - 内容过滤拦截器

过滤不当内容：

```java
ContentFilterInterceptor filter = new ContentFilterInterceptor(
    new BasicContentFilter(),
    true                         // 是否阻止违规内容
);
```

### 6. AuthInterceptor - 认证拦截器

确保API密钥存在：

```java
AuthInterceptor auth = new AuthInterceptor();
auth.registerKey("openai", apiKey);
```

### 组合使用多个拦截器

```java
InterceptorChain chain = InterceptorChain.builder()
    .addInterceptor(new LoggingInterceptor())
    .addInterceptor(new MetricsInterceptor(metrics))
    .addInterceptor(new CachingInterceptor())
    .addInterceptor(new RetryInterceptor())
    .addInterceptor(new RateLimitInterceptor(10, Duration.ofMinutes(1)))
    .addInterceptor(new CircuitBreakerInterceptor(5, Duration.ofSeconds(30)))
    .addInterceptor(new ContentFilterInterceptor(new BasicContentFilter(), false))
    .addInterceptor(new ErrorHandlerInterceptor())
    .build();
```

---

## 增强的工具系统

### 工具超时控制

工具执行支持超时：

```java
ToolExecutor executor = new ToolExecutor(registry, Duration.ofSeconds(30));
ToolResult result = executor.execute(toolCall, Duration.ofSeconds(10));
```

### 批量工具执行

并行执行多个工具：

```java
List<ToolCall> calls = Arrays.asList(call1, call2, call3);
Map<ToolCall, ToolResult> results = executor.executeBatch(calls);
```

### 工具验证

自动验证工具定义和参数：

```java
ToolValidator validator = new ToolValidator();
validator.validate(tool);
validator.validateArguments(tool, arguments);
```

---

## 会话池化

高级会话池实现，支持健康检查和自动清理：

```java
SessionPool pool = new SessionPool(
    factory,
    config,
    5,                              // 最小空闲会话数
    20,                             // 最大会话数
    Duration.ofSeconds(10),         // 最大等待时间
    SessionPool.EvictionPolicy.LRU, // 驱逐策略
    Duration.ofHours(1)             // 会话TTL
);

// 借用会话
LLMSession session = pool.borrow();
try {
    // 使用会话
    session.send(UserMessage.of("Hello"));
} finally {
    // 归还会话
    pool.release(session);
}
```

**特性：**
- 自动会话创建和销毁
- 过期会话自动清理
- 健康检查
- 统计信息：总会话数、借出数、空闲数
- 多种驱逐策略：FIFO, LRU, LFU, TTL

**池统计：**

```java
System.out.println("Total: " + pool.getTotalSessions());
System.out.println("Borrowed: " + pool.getBorrowedSessions());
System.out.println("Idle: " + pool.getIdleSessions());
```

---

## 监控和度量

### 详细的指标收集

Metrics类支持多维度指标：

```java
Metrics metrics = new Metrics();

// 计数器
metrics.increment("requests.total");
metrics.incrementBy("tokens.used", 1000);

// 延迟统计
metrics.recordLatency("openai", nanos);
double avgLatency = metrics.getAverageLatencyMillis("openai");

// Token统计
metrics.recordTokens("openai", usage);
long totalTokens = metrics.getTotalTokens("openai");
```

### 指标报告

生成详细的指标报告：

```java
MetricsReporter reporter = new MetricsReporter(metrics);

// 控制台报告
reporter.report();

// 文本报告
String textReport = reporter.generateReport();

// JSON报告
String jsonReport = reporter.generateJsonReport();
```

### 内置指标

框架自动收集以下指标：

- `requests.total` - 总请求数
- `requests.success` - 成功请求数
- `requests.failure` - 失败请求数
- `requests.provider.*` - 按provider分类的请求
- `latency.*` - 延迟统计
- Token使用量统计

---

## 提示模板引擎

创建可复用的提示模板：

```java
PromptTemplate template = PromptTemplate.from(
    "你是一个{{role}}，请帮助用户{{task}}。"
);

Map<String, Object> vars = new HashMap<>();
vars.put("role", "专业翻译");
vars.put("task", "翻译文档");

String prompt = template.render(vars);
```

### 快速渲染

```java
String prompt = PromptTemplate.quickRender(
    "Hello {{name}}, you have {{count}} messages",
    "name", "Alice",
    "count", "5"
);
```

---

## 安全特性

### 内容过滤

```java
BasicContentFilter filter = new BasicContentFilter();
String filtered = filter.filter(text);
boolean hasViolation = filter.isViolation(text);
```

### 数据脱敏

自动脱敏敏感信息：

```java
DataMasker masker = new DataMasker();
String masked = masker.mask("My email is test@example.com and phone is 123-456-7890");
// 输出: "My email is [EMAIL] and phone is [PHONE]"
```

支持脱敏的类型：
- 邮箱地址 → `[EMAIL]`
- 电话号码 → `[PHONE]`
- 信用卡号 → `[CARD]`

### 审计日志

记录敏感操作：

```java
AuditLog auditLog = new AuditLog();
auditLog.record("SESSION_CREATED", "User: admin, SessionId: xxx");
```

---

## 最佳实践

### 1. 生产环境配置

```java
SessionFactory factory = new SessionFactory();

InterceptorChain chain = InterceptorChain.builder()
    // 日志和监控
    .addInterceptor(new LoggingInterceptor())
    .addInterceptor(new MetricsInterceptor(factory.getGlobalMetrics()))
    
    // 性能优化
    .addInterceptor(new CachingInterceptor())
    
    // 可靠性
    .addInterceptor(new RetryInterceptor())
    .addInterceptor(new CircuitBreakerInterceptor(5, Duration.ofSeconds(30)))
    
    // 限流
    .addInterceptor(new RateLimitInterceptor(100, Duration.ofMinutes(1)))
    
    // 安全
    .addInterceptor(new ContentFilterInterceptor(new BasicContentFilter(), true))
    .addInterceptor(new AuthInterceptor())
    
    // 错误处理
    .addInterceptor(new ErrorHandlerInterceptor())
    .build();
```

### 2. 开发环境配置

```java
InterceptorChain chain = InterceptorChain.builder()
    .addInterceptor(new LoggingInterceptor())
    .addInterceptor(new MetricsInterceptor(metrics))
    .build();
```

### 3. 成本优化

使用缓存减少API调用：

```java
InterceptorChain chain = InterceptorChain.builder()
    .addInterceptor(new CachingInterceptor(
        Duration.ofHours(24),  // 长时间缓存
        5000                   // 大缓存
    ))
    .build();
```

### 4. 监控和告警

```java
Metrics metrics = factory.getGlobalMetrics();
MetricsReporter reporter = new MetricsReporter(metrics);

// 定期检查指标
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
scheduler.scheduleAtFixedRate(() -> {
    long failures = metrics.get("requests.failure");
    if (failures > 100) {
        // 发送告警
        alerting.send("High failure rate: " + failures);
    }
}, 0, 5, TimeUnit.MINUTES);
```

---

## 性能调优

### 连接池配置

```java
// OkHttp连接池在HttpClient中自动配置
// 默认配置：最大空闲连接5个，保持时间5分钟
```

### 会话池配置

```java
SessionPool pool = new SessionPool(
    factory, config,
    10,    // minIdle - 根据负载调整
    50,    // maxTotal - 根据并发需求调整
    Duration.ofSeconds(5),
    SessionPool.EvictionPolicy.LRU,
    Duration.ofMinutes(30)  // 根据使用模式调整
);
```

### 缓存策略

```java
// 高频相同请求场景
CachingInterceptor cache = new CachingInterceptor(
    Duration.ofHours(1),  // 长TTL
    10000                 // 大容量
);

// 低重复率场景
CachingInterceptor cache = new CachingInterceptor(
    Duration.ofMinutes(5),  // 短TTL
    100                     // 小容量
);
```

---

## 故障排查

### 查看指标

```java
Metrics metrics = factory.getGlobalMetrics();
System.out.println("Total: " + metrics.get("requests.total"));
System.out.println("Success: " + metrics.get("requests.success"));
System.out.println("Failure: " + metrics.get("requests.failure"));
System.out.println("Avg Latency: " + metrics.getAverageLatencyMillis("openai") + "ms");
```

### 检查熔断器状态

```java
CircuitBreakerInterceptor cb = ...; // 保存引用
System.out.println("Circuit Breaker State: " + cb.getState());
```

### 查看限流状态

```java
RateLimitInterceptor rl = ...; // 保存引用
System.out.println("Current requests: " + rl.getCurrentRequestCount());
```

### 缓存统计

```java
CachingInterceptor cache = ...; // 保存引用
System.out.println("Cache size: " + cache.getCacheSize());
```

---

## 更多示例

查看 `examples/` 目录获取完整示例：

- `AdvancedInterceptorExample.java` - 多拦截器组合使用
- `SessionPoolExample.java` - 会话池化示例
- `MonitoringExample.java` - 监控和指标示例
- `ToolExecutionExample.java` - 高级工具执行示例

---

## 扩展性

框架设计遵循开闭原则，易于扩展：

### 自定义拦截器

```java
public class CustomInterceptor implements Interceptor {
    @Override
    public Response intercept(Request request, Chain chain, ProviderAdapter adapter) {
        // 前置处理
        Response response = chain.proceed(request);
        // 后置处理
        return response;
    }
}
```

### 自定义Provider

```java
public class CustomProviderAdapter implements ProviderAdapter {
    // 实现接口方法
}

factory.registerProvider(ProviderType.CUSTOM, new CustomProviderAdapter());
```

### 自定义消息历史策略

```java
public class CustomMessageHistory implements MessageHistory {
    // 实现接口方法
}
```

---

## 结论

Session-Based LLM Framework提供了企业级的功能和性能：

✅ **可靠性**: 重试、熔断器、错误处理  
✅ **性能**: 缓存、连接池、会话池  
✅ **可观测性**: 详细指标、日志、追踪  
✅ **安全性**: 内容过滤、数据脱敏、审计  
✅ **可扩展性**: 插件化架构、自定义组件  

适用于各种规模的LLM应用开发。
