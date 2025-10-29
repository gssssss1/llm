# Session、事件驱动与上下文管理 - 完整指南

## 概述

本文档介绍基于事件驱动的 Session 系统，包含记忆管理和上下文管理功能。

## 🎯 核心概念

### 1. Session (会话)
Session 代表一次完整的对话会话，包含：
- 会话ID和用户ID
- 消息历史
- 事件流
- 记忆系统
- 上下文管理

### 2. Event (事件)
所有会话中的活动都以事件形式记录：
- 用户消息 (UserMessageEvent)
- 助手回复 (AssistantMessageEvent)
- 工具调用 (ToolCallEvent)
- 工具结果 (ToolResultEvent)
- 思考过程 (ThinkingEvent)

### 3. Memory (记忆)
分为短期和长期记忆：
- ShortTermMemory: 临时存储，有大小限制
- LongTermMemory: 持久化存储（可扩展）

### 4. ContextManager (上下文管理)
管理对话上下文：
- Token 计数和限制
- 消息查询和过滤
- 上下文压缩
- 消息删除和添加

---

## 快速开始

### 基本用法

```java
// 1. 创建客户端和工具执行器
OpenAIClient client = OpenAIClient.builder()
    .apiKey("your-api-key")
    .build();

ToolExecutor toolExecutor = new ToolExecutor();
toolExecutor.registerTool(new MyTools());

// 2. 创建 Session Manager
SessionManager sessionManager = new SessionManager();

// 3. 创建 Session
Session session = sessionManager.createSession(
    Session.builder()
        .userId("user_123")
        .client(client)
        .model("gpt-3.5-turbo")
        .toolExecutor(toolExecutor)
        .systemPrompt("You are a helpful assistant.")
);

// 4. 发送消息
String response = session.sendMessage("Hello, how are you?");
System.out.println(response);
```

### 添加事件监听器

```java
session.addEventListener(new EventHandler() {
    @Override
    public void onEvent(Event event) {
        System.out.println("Event: " + event.getType());
    }
    
    @Override
    public void onUserMessage(UserMessageEvent event) {
        System.out.println("User: " + event.getContent());
    }
    
    @Override
    public void onAssistantMessage(AssistantMessageEvent event) {
        System.out.println("Assistant: " + event.getContent());
        System.out.println("Tokens: " + event.getTokensUsed());
    }
    
    @Override
    public void onToolCall(ToolCallEvent event) {
        System.out.println("Tool: " + event.getToolName());
    }
    
    @Override
    public void onToolResult(ToolResultEvent event) {
        if (event.isSuccess()) {
            System.out.println("Result: " + event.getResult());
        } else {
            System.out.println("Error: " + event.getError());
        }
    }
});
```

---

## Event System (事件系统)

### 事件类型

#### 1. UserMessageEvent
用户发送消息时触发：

```java
public class UserMessageEvent extends Event {
    private final String content;
    private final String userId;
}
```

#### 2. AssistantMessageEvent
助手回复时触发：

```java
public class AssistantMessageEvent extends Event {
    private final String content;
    private final String model;
    private final int tokensUsed;
}
```

#### 3. ToolCallEvent
调用工具时触发：

```java
public class ToolCallEvent extends Event {
    private final String toolName;
    private final String toolCallId;
    private final String arguments;
}
```

#### 4. ToolResultEvent
工具返回结果时触发：

```java
public class ToolResultEvent extends Event {
    private final String toolCallId;
    private final String toolName;
    private final String result;
    private final boolean success;
    private final String error;
}
```

#### 5. ThinkingEvent
记录思考过程：

```java
public class ThinkingEvent extends Event {
    private final String thought;
    private final String reasoning;
}
```

### 事件处理器

```java
public interface EventHandler {
    void onEvent(Event event);
    void onUserMessage(UserMessageEvent event);
    void onAssistantMessage(AssistantMessageEvent event);
    void onToolCall(ToolCallEvent event);
    void onToolResult(ToolResultEvent event);
    void onThinking(ThinkingEvent event);
}
```

### 示例：日志记录处理器

```java
public class LoggingEventHandler implements EventHandler {
    @Override
    public void onEvent(Event event) {
        System.out.println(String.format(
            "[%s] %s at %s",
            event.getSessionId(),
            event.getType(),
            event.getTimestamp()
        ));
    }
    
    @Override
    public void onAssistantMessage(AssistantMessageEvent event) {
        System.out.println("Tokens used: " + event.getTokensUsed());
    }
}
```

---

## Memory System (记忆系统)

### Memory 接口

```java
public interface Memory {
    void add(Message message);
    void add(String key, String value);
    String get(String key);
    List<Message> getMessages();
    List<Message> getRecentMessages(int count);
    void clear();
    int size();
}
```

### ShortTermMemory (短期记忆)

```java
// 创建短期记忆，最多保存 100 条消息
Memory memory = new ShortTermMemory(100);

// 添加消息
memory.add(new UserMessage("Hello"));

// 存储键值对
memory.add("user_preference", "dark_mode");
String preference = memory.get("user_preference");

// 获取最近的消息
List<Message> recent = memory.getRecentMessages(10);
```

### 自定义 Session 的记忆

```java
Session session = Session.builder()
    .client(client)
    .model("gpt-3.5-turbo")
    .memory(new ShortTermMemory(50))  // 自定义记忆大小
    .build();
```

---

## Context Management (上下文管理)

### ContextManager

```java
// 创建上下文管理器，最大 4096 tokens
ContextManager contextManager = new ContextManager(4096);

// 添加消息
contextManager.addMessage(new UserMessage("Hello"));

// 获取所有消息
List<Message> messages = contextManager.getMessages();

// 获取总 token 数
int totalTokens = contextManager.getTotalTokens();
```

### 查询消息

使用 MessageFilter 查询特定消息：

```java
// 按角色查询
List<Message> userMessages = contextManager.query(
    MessageFilter.byRole(MessageRole.USER)
);

// 按内容查询
List<Message> weatherMessages = contextManager.query(
    MessageFilter.byContent("weather")
);

// 组合查询
List<Message> filtered = contextManager.query(
    MessageFilter.and(
        MessageFilter.byRole(MessageRole.ASSISTANT),
        MessageFilter.byContent("Tokyo")
    )
);
```

### 删除消息

```java
// 删除特定位置的消息
contextManager.deleteMessage(0);

// 按条件删除
contextManager.deleteMessages(
    MessageFilter.byRole(MessageRole.SYSTEM)
);

// 清空所有消息
contextManager.clear();
```

### 压缩上下文

当消息过多超过 token 限制时，自动压缩：

```java
// 压缩到 token 限制内
List<Message> compressed = contextManager.compress();

// 上下文管理器会自动压缩
// 删除最旧的消息以保持在限制内
contextManager.addMessage(newMessage);  // 自动压缩
```

### 自定义 Token 计数器

```java
// 创建自定义 token 计数器
public class MyTokenCounter implements TokenCounter {
    @Override
    public int count(String text) {
        // 实现自定义的 token 计数逻辑
        return text.split("\\s+").length;
    }
}

// 使用自定义计数器
ContextManager contextManager = new ContextManager(
    4096,
    new MyTokenCounter()
);
```

---

## Session Management (会话管理)

### SessionManager

```java
// 创建会话管理器，超时时间 1 小时
SessionManager sessionManager = new SessionManager(Duration.ofHours(1));

// 创建会话
Session session = sessionManager.createSession(
    Session.builder()
        .userId("user_123")
        .client(client)
        .model("gpt-3.5-turbo")
        .toolExecutor(toolExecutor)
);

// 获取会话
Session retrieved = sessionManager.getSession(session.getSessionId());

// 获取用户的所有会话
List<Session> userSessions = sessionManager.getUserSessions("user_123");

// 清理过期会话
sessionManager.clearExpiredSessions();

// 获取活跃会话数
int activeCount = sessionManager.getActiveSessionCount();
```

### 会话生命周期

```java
Session session = sessionManager.createSession(...);

// 会话信息
String sessionId = session.getSessionId();
String userId = session.getUserId();
Instant createdAt = session.getCreatedAt();
Instant lastActivityAt = session.getLastActivityAt();
boolean isActive = session.isActive();

// 关闭会话
session.close();
```

---

## 完整示例

### 示例 1: 基本对话

```java
public class BasicConversation {
    public static void main(String[] args) throws Exception {
        OpenAIClient client = OpenAIClient.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .build();
        
        Session session = Session.builder()
            .userId("user_001")
            .client(client)
            .model("gpt-3.5-turbo")
            .systemPrompt("You are a helpful assistant.")
            .build();
        
        try {
            String response1 = session.sendMessage("What is 2+2?");
            System.out.println(response1);
            
            String response2 = session.sendMessage("And what is that times 3?");
            System.out.println(response2);
            
            // 查看对话历史
            System.out.println("Messages: " + session.getConversation().size());
            
        } finally {
            client.close();
        }
    }
}
```

### 示例 2: 带工具的会话

```java
public class ToolEnabledSession {
    public static void main(String[] args) throws Exception {
        OpenAIClient client = OpenAIClient.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .build();
        
        ToolExecutor toolExecutor = new ToolExecutor();
        toolExecutor.registerTool(new WeatherTools());
        toolExecutor.registerTool(new CalculatorTools());
        
        Session session = Session.builder()
            .userId("user_001")
            .client(client)
            .model("gpt-4")
            .toolExecutor(toolExecutor)
            .systemPrompt("You are an assistant with access to tools.")
            .build();
        
        // 监听工具调用
        session.addEventListener(new EventHandler() {
            @Override
            public void onEvent(Event event) {
                System.out.println("[" + event.getType() + "]");
            }
            
            @Override
            public void onToolCall(ToolCallEvent event) {
                System.out.println("Calling tool: " + event.getToolName());
            }
        });
        
        try {
            String response = session.sendMessage(
                "What's the weather in Paris and calculate 15 * 7?"
            );
            System.out.println(response);
            
        } finally {
            client.close();
        }
    }
}
```

### 示例 3: 上下文管理

```java
public class ContextManagementExample {
    public static void main(String[] args) throws Exception {
        OpenAIClient client = OpenAIClient.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .build();
        
        // 创建有限上下文的会话
        ContextManager contextManager = new ContextManager(2048);
        
        Session session = Session.builder()
            .userId("user_001")
            .client(client)
            .model("gpt-3.5-turbo")
            .contextManager(contextManager)
            .build();
        
        try {
            // 发送多条消息
            for (int i = 0; i < 10; i++) {
                session.sendMessage("Message " + i);
            }
            
            // 查看上下文信息
            System.out.println("Messages: " + contextManager.size());
            System.out.println("Tokens: " + contextManager.getTotalTokens());
            
            // 查询用户消息
            List<Message> userMessages = contextManager.query(
                MessageFilter.byRole(MessageRole.USER)
            );
            System.out.println("User messages: " + userMessages.size());
            
            // 压缩上下文
            List<Message> compressed = contextManager.compress();
            System.out.println("Compressed: " + compressed.size());
            
        } finally {
            client.close();
        }
    }
}
```

### 示例 4: 多会话管理

```java
public class MultiSessionExample {
    public static void main(String[] args) throws Exception {
        OpenAIClient client = OpenAIClient.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .build();
        
        SessionManager manager = new SessionManager(Duration.ofMinutes(30));
        
        // 为不同用户创建会话
        Session session1 = manager.createSession(
            Session.builder()
                .userId("user_001")
                .client(client)
                .model("gpt-3.5-turbo")
        );
        
        Session session2 = manager.createSession(
            Session.builder()
                .userId("user_002")
                .client(client)
                .model("gpt-3.5-turbo")
        );
        
        try {
            // 用户 1 对话
            session1.sendMessage("Hello from user 1");
            
            // 用户 2 对话
            session2.sendMessage("Hello from user 2");
            
            // 查看统计
            System.out.println("Active sessions: " + manager.getActiveSessionCount());
            System.out.println("User 1 sessions: " + manager.getUserSessions("user_001").size());
            
            // 清理过期会话
            Thread.sleep(Duration.ofMinutes(31).toMillis());
            manager.clearExpiredSessions();
            
        } finally {
            client.close();
        }
    }
}
```

---

## 最佳实践

### 1. 事件处理

```java
// 创建专门的事件处理器
public class AuditEventHandler implements EventHandler {
    private final AuditLogger logger;
    
    @Override
    public void onEvent(Event event) {
        logger.log(event);
    }
}

session.addEventListener(new AuditEventHandler());
session.addEventListener(new MetricsEventHandler());
session.addEventListener(new NotificationEventHandler());
```

### 2. 记忆管理

```java
// 定期保存记忆到持久化存储
Memory memory = session.getMemory();
List<Message> messages = memory.getMessages();
database.save(sessionId, messages);

// 从存储恢复记忆
List<Message> storedMessages = database.load(sessionId);
for (Message msg : storedMessages) {
    memory.add(msg);
}
```

### 3. 上下文优化

```java
// 智能压缩：保留重要消息
List<Message> important = contextManager.query(
    MessageFilter.or(
        MessageFilter.byRole(MessageRole.SYSTEM),
        MessageFilter.byContent("important")
    )
);

// 删除临时消息
contextManager.deleteMessages(
    MessageFilter.byContent("temporary")
);
```

### 4. Session 清理

```java
// 定期清理任务
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
scheduler.scheduleAtFixedRate(
    () -> sessionManager.clearExpiredSessions(),
    1, 1, TimeUnit.HOURS
);
```

---

## 架构图

```
User Input
    ↓
Session.sendMessage()
    ↓
Emit UserMessageEvent
    ↓
Add to Conversation/Context/Memory
    ↓
Compress Context
    ↓
LLM API Call
    ↓
Check Tool Calls
    ↓
Emit ToolCallEvent
    ↓
Execute Tools
    ↓
Emit ToolResultEvent
    ↓
Follow-up API Call
    ↓
Emit AssistantMessageEvent
    ↓
Update Conversation/Context/Memory
    ↓
Return Response
```

---

## 总结

### 核心优势

1. **事件驱动**: 所有活动可追踪和审计
2. **灵活记忆**: 支持短期和长期存储
3. **智能上下文**: 自动管理 token 限制
4. **会话管理**: 支持多用户多会话
5. **可扩展**: 易于添加自定义功能

### 适用场景

- ✅ 聊天机器人
- ✅ AI 助手
- ✅ 客服系统
- ✅ 教育应用
- ✅ 企业知识库
- ✅ 多轮对话应用

现在你可以构建强大的对话式 AI 应用！🚀
