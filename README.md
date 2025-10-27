# Session-Based LLM Framework

A simple, efficient, and elegant Java framework for interacting with Large Language Models (LLMs) through a session-centric architecture.

## Features

- **Session-Centric Design**: Conversations are managed through intuitive sessions with state management
- **Multi-Provider Support**: Built-in adapters for OpenAI and Anthropic with easy extensibility
- **Flexible Execution Modes**: Synchronous, asynchronous, streaming, and batch processing
- **Message History Management**: Multiple strategies (FULL, SLIDING_WINDOW, TOKEN_LIMIT, SUMMARY)
- **Tool Calling System**: Register and execute custom tools/functions
- **Interceptor Chain**: Extensible middleware for logging, metrics, caching, auth, etc.
- **Session Persistence**: Save and restore conversation state
- **Session Pooling**: Reuse sessions efficiently
- **Cost Tracking**: Monitor token usage and API costs
- **Type-Safe API**: Leverages Java's type system for compile-time safety

## Quick Start

### Prerequisites

- Java 11 or higher
- Maven 3.6+
- API keys for your chosen provider (OpenAI, Anthropic)

### Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.llm</groupId>
    <artifactId>session-based-llm-framework</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Or build from source:

```bash
git clone https://github.com/yourusername/session-based-llm-framework.git
cd session-based-llm-framework
mvn clean install
```

### Basic Usage

```java
import com.llm.core.message.UserMessage;
import com.llm.core.session.LLMSession;
import com.llm.core.session.SessionFactory;
import com.llm.provider.ProviderType;
import com.llm.provider.common.Response;

// Set up your API key
System.setenv("OPENAI_API_KEY", "your-api-key-here");

// Create a session
SessionFactory factory = new SessionFactory();
try (LLMSession session = factory.builder()
        .provider(ProviderType.OPENAI)
        .model("gpt-3.5-turbo")
        .systemPrompt("You are a helpful assistant.")
        .build()) {

    // Send a message
    Response response = session.send(UserMessage.of("Hello!"));
    System.out.println(response.getMessage().getTextContent());
}
```

## Core Concepts

### 1. Sessions

A `LLMSession` represents an ongoing conversation with an LLM. Sessions:
- Maintain conversation history
- Track state (IDLE, RUNNING, CLOSED)
- Support multiple execution modes
- Can be forked, saved, and restored

```java
LLMSession session = factory.create(ProviderType.OPENAI, "gpt-3.5-turbo");
```

### 2. Messages

Messages are strongly-typed and support multimodal content:

```java
UserMessage userMsg = UserMessage.of("Hello");
SystemMessage sysMsg = SystemMessage.of("You are a helpful assistant");
AssistantMessage assistantMsg = AssistantMessage.of("Hi there!");
ToolMessage toolMsg = ToolMessage.of("tool-123", "weather", "Sunny, 25°C");
```

### 3. Session Context

Context holds conversation state:
- **Message History**: Full or windowed history
- **System Prompt**: Define assistant behavior
- **Variables**: Store custom context
- **Metadata**: Arbitrary key-value data

```java
SessionContext context = SessionContext.builder()
    .systemPrompt("You are a helpful assistant")
    .variable("user_id", "12345")
    .metadata("session_type", "support")
    .build();
```

### 4. Configuration

Configure model parameters, execution, features, and security:

```java
ModelConfig modelConfig = ModelConfig.builder()
    .provider("openai")
    .model("gpt-4")
    .parameter("temperature", 0.7)
    .parameter("max_tokens", 1000)
    .build();

ExecutionConfig execConfig = ExecutionConfig.builder()
    .timeout(Duration.ofSeconds(60))
    .maxRetries(3)
    .build();

SessionConfig config = SessionConfig.builder()
    .modelConfig(modelConfig)
    .executionConfig(execConfig)
    .build();
```

## Execution Modes

### Synchronous

```java
Response response = session.send(UserMessage.of("Hello"));
```

### Asynchronous

```java
CompletableFuture<Response> future = session.sendAsync(UserMessage.of("Hello"));
future.thenAccept(response -> System.out.println(response.getMessage().getTextContent()));
```

### Streaming

```java
session.sendStream(UserMessage.of("Tell me a story"), chunk -> {
    System.out.print(chunk);
});
```

### Batch Processing

```java
List<Message> messages = Arrays.asList(
    UserMessage.of("Question 1"),
    UserMessage.of("Question 2")
);
List<Response> responses = session.sendBatch(messages);
```

## Tool Calling

Register custom tools that the LLM can call:

```java
public class WeatherTool implements Tool {
    @Override
    public String getName() {
        return "get_weather";
    }

    @Override
    public String getDescription() {
        return "Gets weather for a city";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of("city", Map.of("type", "string"))
        );
    }

    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        String city = (String) arguments.get("city");
        return ToolResult.builder()
            .content("Sunny, 25°C")
            .build();
    }
}

ToolRegistry registry = new ToolRegistry();
registry.register(new WeatherTool());
```

## Interceptors

Add middleware to requests:

```java
InterceptorChain chain = InterceptorChain.builder()
    .addInterceptor(new LoggingInterceptor())
    .addInterceptor(new MetricsInterceptor(metrics))
    .build();

LLMSession session = factory.builder()
    .provider(ProviderType.OPENAI)
    .model("gpt-3.5-turbo")
    .interceptorChain(chain)
    .build();
```

Built-in interceptors:
- `LoggingInterceptor`: Logs requests and responses
- `MetricsInterceptor`: Collects performance metrics
- `CachingInterceptor`: Caches responses (planned)
- `RetryInterceptor`: Automatic retry logic (planned)

## Session Persistence

Save and restore session state:

```java
// Save
session.save("my-session.json");

// Restore
LLMSession restored = factory.restore("my-session.json");
```

## Session Lifecycle

```java
// Fork a session (creates independent copy)
LLMSession forked = session.fork();

// Clear history
session.clearHistory();

// Rollback last N messages
session.rollback(2);

// Reset to initial state
session.reset();

// Close when done
session.close();
```

## Supported Providers

### OpenAI

```java
SessionFactory factory = new SessionFactory();
factory.registerProvider(ProviderType.OPENAI, new OpenAIAdapter("your-api-key"));
```

Supported models:
- `gpt-3.5-turbo`
- `gpt-4`
- `gpt-4-turbo`
- `gpt-4o`

### Anthropic

```java
factory.registerProvider(ProviderType.ANTHROPIC, new AnthropicAdapter("your-api-key"));
```

Supported models:
- `claude-2.1`
- `claude-3-sonnet`
- `claude-3-opus`

## Configuration Files

Load configuration from YAML:

```yaml
provider:
  type: openai
  model: gpt-3.5-turbo

execution:
  timeoutMillis: 60000
  maxRetries: 3
  retryDelayMillis: 1000

features:
  toolCalling: true
  streaming: true

security:
  contentFilter: false
  dataMasking: false
  auditLog: false
```

## Examples

See the `examples/` directory for complete examples:

- `SimpleConversationExample`: Basic chat
- `MultiRoundConversationExample`: Multi-turn dialogue
- `StreamingExample`: Streaming responses
- `AsyncExample`: Asynchronous requests
- `ToolCallingExample`: Function calling
- `PersistenceExample`: Save/restore sessions
- `InterceptorExample`: Custom middleware
- `BatchProcessingExample`: Batch requests

## Architecture

```
┌─────────────────────────────────────────────┐
│           Application Layer                 │
│  (Your Code Using LLMSession)              │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│          Core Session Layer                 │
│  ┌───────────┐  ┌──────────────┐           │
│  │ LLMSession│  │SessionFactory│           │
│  └─────┬─────┘  └──────────────┘           │
│        │                                    │
│  ┌─────▼──────┐  ┌──────────────┐          │
│  │   Context  │  │ Interceptors │          │
│  └────────────┘  └──────────────┘          │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│         Provider Adapter Layer              │
│  ┌──────────┐  ┌──────────────┐            │
│  │  OpenAI  │  │  Anthropic   │            │
│  └────┬─────┘  └──────┬───────┘            │
└───────┼────────────────┼────────────────────┘
        │                │
┌───────▼────────────────▼────────────────────┐
│          Transport Layer                    │
│  ┌────────────┐  ┌──────────────┐          │
│  │ HttpClient │  │  SSE Parser  │          │
│  └────────────┘  └──────────────┘          │
└─────────────────────────────────────────────┘
```

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch (`feature/session-based-framework`)
3. Make your changes
4. Write tests
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For issues, questions, or contributions:
- GitHub Issues: https://github.com/yourusername/session-based-llm-framework/issues
- Documentation: https://docs.example.com

## Roadmap

- [ ] Additional provider adapters (Azure OpenAI, Google PaLM)
- [ ] Enhanced caching strategies
- [ ] Circuit breaker implementation
- [ ] Rate limiting
- [ ] Content filtering
- [ ] Token counting improvements
- [ ] Session pooling
- [ ] Distributed session storage
