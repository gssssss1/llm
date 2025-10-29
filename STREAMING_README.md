# 流式返回 (Streaming) - 使用指南

## 概述

流式返回允许您实时接收 LLM 的响应，而不是等待完整响应。这提供了更好的用户体验，特别是对于长文本生成。

## 核心特性

✅ **实时响应**: 逐块接收内容  
✅ **OpenAI 支持**: 完整的 GPT-3.5/GPT-4 流式支持  
✅ **Anthropic 支持**: Claude 流式支持  
✅ **Session 集成**: 与会话系统无缝集成  
✅ **事件驱动**: 基于回调的异步处理  
✅ **错误处理**: 完整的错误处理机制  

---

## StreamCallback 接口

```java
public interface StreamCallback {
    void onStart();                      // 流开始
    void onChunk(String content);        // 接收到内容块
    void onComplete(String fullContent); // 流完成
    void onError(Exception error);       // 发生错误
}
```

---

## 快速开始

### 1. 基本流式调用

```java
OpenAIClient client = OpenAIClient.builder()
    .apiKey("your-api-key")
    .build();

ChatRequest request = ChatRequestBuilder.create("gpt-3.5-turbo")
    .message(MessageBuilder.user("Tell me a story"))
    .build();

client.chatStream(request, new StreamCallback() {
    @Override
    public void onStart() {
        System.out.println("Streaming started...");
    }
    
    @Override
    public void onChunk(String content) {
        System.out.print(content);  // 实时打印
    }
    
    @Override
    public void onComplete(String fullContent) {
        System.out.println("\nStreaming completed!");
    }
    
    @Override
    public void onError(Exception error) {
        System.err.println("Error: " + error.getMessage());
    }
});
```

### 2. 使用 CountDownLatch 等待完成

```java
CountDownLatch latch = new CountDownLatch(1);
StringBuilder fullResponse = new StringBuilder();

client.chatStream(request, new StreamCallback() {
    @Override
    public void onStart() {
        // 准备接收
    }
    
    @Override
    public void onChunk(String content) {
        fullResponse.append(content);
        System.out.print(content);
    }
    
    @Override
    public void onComplete(String fullContent) {
        System.out.println("\nDone!");
        latch.countDown();
    }
    
    @Override
    public void onError(Exception error) {
        error.printStackTrace();
        latch.countDown();
    }
});

latch.await();  // 等待流式完成
System.out.println("Full response: " + fullResponse.toString());
```

---

## OpenAI 流式调用

### GPT-3.5/GPT-4

```java
OpenAIClient client = OpenAIClient.builder()
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .build();

ChatRequest request = ChatRequestBuilder.create("gpt-3.5-turbo")
    .message(MessageBuilder.system("You are a helpful assistant."))
    .message(MessageBuilder.user("Explain quantum computing in simple terms."))
    .temperature(0.7)
    .maxTokens(500)
    .build();

client.chatStream(request, new StreamCallback() {
    private int chunkCount = 0;
    
    @Override
    public void onStart() {
        System.out.println("Assistant: ");
    }
    
    @Override
    public void onChunk(String content) {
        chunkCount++;
        System.out.print(content);
        System.out.flush();
    }
    
    @Override
    public void onComplete(String fullContent) {
        System.out.println("\n");
        System.out.println("Received " + chunkCount + " chunks");
        System.out.println("Total characters: " + fullContent.length());
    }
    
    @Override
    public void onError(Exception error) {
        System.err.println("Streaming error: " + error.getMessage());
    }
});
```

---

## Anthropic 流式调用

### Claude 3

```java
AnthropicClient client = AnthropicClient.builder()
    .apiKey(System.getenv("ANTHROPIC_API_KEY"))
    .build();

ChatRequest request = ChatRequestBuilder.create("claude-3-opus-20240229")
    .message(MessageBuilder.user("Write a short poem about AI"))
    .maxTokens(1024)
    .build();

client.chatStream(request, new StreamCallback() {
    @Override
    public void onStart() {
        System.out.println("Claude is thinking...\n");
    }
    
    @Override
    public void onChunk(String content) {
        System.out.print(content);
    }
    
    @Override
    public void onComplete(String fullContent) {
        System.out.println("\n\nPoem completed!");
    }
    
    @Override
    public void onError(Exception error) {
        error.printStackTrace();
    }
});
```

---

## Session 流式调用

### 基本用法

```java
Session session = Session.builder()
    .userId("user_123")
    .client(client)
    .model("gpt-3.5-turbo")
    .systemPrompt("You are helpful")
    .build();

session.sendMessageStream("Tell me a joke", new StreamCallback() {
    @Override
    public void onStart() {
        System.out.print("Assistant: ");
    }
    
    @Override
    public void onChunk(String content) {
        System.out.print(content);
    }
    
    @Override
    public void onComplete(String fullContent) {
        System.out.println();
    }
    
    @Override
    public void onError(Exception error) {
        error.printStackTrace();
    }
});
```

### 与事件系统集成

```java
Session session = Session.builder()
    .client(client)
    .model("gpt-3.5-turbo")
    .build();

session.addEventListener(new EventHandler() {
    @Override
    public void onUserMessage(UserMessageEvent event) {
        System.out.println("User: " + event.getContent());
    }
    
    @Override
    public void onAssistantMessage(AssistantMessageEvent event) {
        System.out.println("\nFull response saved to context");
    }
});

session.sendMessageStream(
    "What's the weather like?",
    new StreamCallback() {
        @Override
        public void onStart() {
            System.out.print("Assistant: ");
        }
        
        @Override
        public void onChunk(String content) {
            System.out.print(content);
            System.out.flush();
        }
        
        @Override
        public void onComplete(String fullContent) {
            System.out.println();
        }
        
        @Override
        public void onError(Exception error) {
            System.err.println("Error: " + error.getMessage());
        }
    }
);
```

---

## 高级用法

### 1. 实时处理和显示

```java
public class RealTimeDisplay {
    
    public static void streamWithFormatting(LLMClient client) throws Exception {
        ChatRequest request = ChatRequestBuilder.create("gpt-3.5-turbo")
            .message(MessageBuilder.user("List 5 programming languages"))
            .build();
        
        CountDownLatch latch = new CountDownLatch(1);
        
        client.chatStream(request, new StreamCallback() {
            private StringBuilder currentLine = new StringBuilder();
            
            @Override
            public void onStart() {
                System.out.println("=".repeat(50));
            }
            
            @Override
            public void onChunk(String content) {
                currentLine.append(content);
                
                // 检测换行
                if (content.contains("\n")) {
                    System.out.println(currentLine.toString().trim());
                    currentLine.setLength(0);
                } else {
                    System.out.print(content);
                    System.out.flush();
                }
            }
            
            @Override
            public void onComplete(String fullContent) {
                if (currentLine.length() > 0) {
                    System.out.println(currentLine.toString());
                }
                System.out.println("=".repeat(50));
                latch.countDown();
            }
            
            @Override
            public void onError(Exception error) {
                System.err.println("\nError: " + error.getMessage());
                latch.countDown();
            }
        });
        
        latch.await();
    }
}
```

### 2. 收集统计信息

```java
public class StreamStatistics {
    
    public static void collectStats(LLMClient client) throws Exception {
        ChatRequest request = ChatRequestBuilder.create("gpt-3.5-turbo")
            .message(MessageBuilder.user("Explain photosynthesis"))
            .build();
        
        CountDownLatch latch = new CountDownLatch(1);
        
        client.chatStream(request, new StreamCallback() {
            private long startTime;
            private int chunkCount = 0;
            private int totalChars = 0;
            
            @Override
            public void onStart() {
                startTime = System.currentTimeMillis();
                System.out.println("Streaming started...");
            }
            
            @Override
            public void onChunk(String content) {
                chunkCount++;
                totalChars += content.length();
                System.out.print(content);
            }
            
            @Override
            public void onComplete(String fullContent) {
                long duration = System.currentTimeMillis() - startTime;
                
                System.out.println("\n\n--- Statistics ---");
                System.out.println("Duration: " + duration + "ms");
                System.out.println("Chunks: " + chunkCount);
                System.out.println("Total characters: " + totalChars);
                System.out.println("Avg chunk size: " + (totalChars / chunkCount));
                System.out.println("Chars/second: " + (totalChars * 1000 / duration));
                
                latch.countDown();
            }
            
            @Override
            public void onError(Exception error) {
                System.err.println("Error: " + error.getMessage());
                latch.countDown();
            }
        });
        
        latch.await();
    }
}
```

### 3. 多流并发

```java
public class ConcurrentStreaming {
    
    public static void parallelStreams(LLMClient client) throws Exception {
        String[] questions = {
            "What is AI?",
            "What is ML?",
            "What is DL?"
        };
        
        CountDownLatch latch = new CountDownLatch(questions.length);
        
        for (int i = 0; i < questions.length; i++) {
            final int index = i;
            final String question = questions[i];
            
            new Thread(() -> {
                try {
                    ChatRequest request = ChatRequestBuilder.create("gpt-3.5-turbo")
                        .message(MessageBuilder.user(question))
                        .maxTokens(100)
                        .build();
                    
                    System.out.println("\n[Stream " + index + "] Started: " + question);
                    
                    client.chatStream(request, new StreamCallback() {
                        private StringBuilder response = new StringBuilder();
                        
                        @Override
                        public void onStart() {
                        }
                        
                        @Override
                        public void onChunk(String content) {
                            response.append(content);
                        }
                        
                        @Override
                        public void onComplete(String fullContent) {
                            System.out.println("\n[Stream " + index + "] Completed");
                            System.out.println("Response: " + response.toString());
                            latch.countDown();
                        }
                        
                        @Override
                        public void onError(Exception error) {
                            System.err.println("[Stream " + index + "] Error: " + error.getMessage());
                            latch.countDown();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    latch.countDown();
                }
            }).start();
        }
        
        latch.await();
        System.out.println("\nAll streams completed");
    }
}
```

---

## 错误处理

### 处理网络错误

```java
client.chatStream(request, new StreamCallback() {
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;
    
    @Override
    public void onStart() {
        System.out.println("Starting stream...");
    }
    
    @Override
    public void onChunk(String content) {
        System.out.print(content);
    }
    
    @Override
    public void onComplete(String fullContent) {
        System.out.println("\nSuccess!");
    }
    
    @Override
    public void onError(Exception error) {
        System.err.println("Error: " + error.getMessage());
        
        if (error.getMessage().contains("timeout") && retryCount < MAX_RETRIES) {
            retryCount++;
            System.out.println("Retrying... (" + retryCount + "/" + MAX_RETRIES + ")");
            // 重试逻辑
        } else {
            System.err.println("Failed after " + retryCount + " retries");
        }
    }
});
```

---

## 最佳实践

### 1. 使用 CountDownLatch

```java
// 等待流式完成
CountDownLatch latch = new CountDownLatch(1);
client.chatStream(request, callback);
latch.await(30, TimeUnit.SECONDS);  // 设置超时
```

### 2. 线程安全

```java
// 使用线程安全的数据结构
final StringBuilder fullContent = new StringBuilder();
final Object lock = new Object();

client.chatStream(request, new StreamCallback() {
    @Override
    public void onChunk(String content) {
        synchronized (lock) {
            fullContent.append(content);
        }
    }
});
```

### 3. 资源管理

```java
try {
    client.chatStream(request, callback);
} finally {
    // 确保清理资源
    client.close();
}
```

### 4. 进度显示

```java
client.chatStream(request, new StreamCallback() {
    private int charCount = 0;
    
    @Override
    public void onChunk(String content) {
        charCount += content.length();
        System.out.print(content);
        
        // 每 50 个字符显示进度
        if (charCount % 50 == 0) {
            System.out.print(" [" + charCount + "]");
        }
    }
});
```

---

## 性能优化

### 1. 缓冲输出

```java
BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

client.chatStream(request, new StreamCallback() {
    @Override
    public void onChunk(String content) throws IOException {
        writer.write(content);
        writer.flush();
    }
});
```

### 2. 批处理块

```java
client.chatStream(request, new StreamCallback() {
    private StringBuilder buffer = new StringBuilder();
    
    @Override
    public void onChunk(String content) {
        buffer.append(content);
        
        // 每 10 个块处理一次
        if (buffer.length() >= 10) {
            processBuffer(buffer.toString());
            buffer.setLength(0);
        }
    }
    
    @Override
    public void onComplete(String fullContent) {
        if (buffer.length() > 0) {
            processBuffer(buffer.toString());
        }
    }
});
```

---

## 总结

### 核心优势

1. **实时响应**: 更好的用户体验
2. **低延迟**: 无需等待完整响应
3. **灵活处理**: 可以实时处理每个块
4. **易于集成**: 与现有系统无缝集成

### 适用场景

- ✅ 聊天应用
- ✅ 实时翻译
- ✅ 代码生成
- ✅ 长文本生成
- ✅ 交互式问答

现在您可以使用流式返回构建更流畅的 AI 应用！🚀
