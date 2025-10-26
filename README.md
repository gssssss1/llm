# Modern Java LLM Framework

A modern, reactive Java framework for LLM application development with comprehensive features and elegant APIs.

## Features

- 🚀 **Reactive First**: Built on Project Reactor for async/non-blocking operations
- 🎯 **Type Safe**: Leverages Java 21+ features (Records, Sealed Classes, Pattern Matching)
- 🔧 **Functional**: Fluent APIs and functional programming patterns
- 📦 **Modular**: Clean architecture with pluggable components
- 🎨 **Multi-Model Support**: Chat, Embedding, and Image generation
- 🛠️ **Tool Calling**: Native function calling support with easy registration
- 💾 **Intelligent Caching**: Multi-tier caching with semantic similarity
- 📊 **Observability**: Built-in metrics, tracing, and cost tracking
- ⚡ **Performance**: Connection pooling, rate limiting, and retry strategies
- 🔌 **Extensible**: Plugin architecture with interceptor chains

## Requirements

- Java 21 or higher
- Maven 3.8+

## Quick Start

### Basic Usage

```java
// Create a chat model
ChatModel chatModel = OpenAIChatModel.builder()
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .modelName("gpt-4")
    .build();

// Simple chat
String response = chatModel.chat("What is the capital of France?").block();
System.out.println(response);

// Async chat
chatModel.chat("Tell me a joke")
    .subscribe(response -> System.out.println(response));

// Streaming chat
chatModel.stream(ChatInput.of("Write a short poem"))
    .subscribe(chunk -> System.out.print(chunk.output().content()));
```

### Structured Output

```java
record Person(String name, int age, String occupation) {}

Person person = chatModel.chatStructured(
    "Extract: John is a 30-year-old software engineer",
    Person.class
).block();
```

### Tool Calling

```java
// Define a tool
Tool weatherTool = Tool.builder("get_weather")
    .description("Get current weather for a location")
    .parameter("location", String.class, "City name")
    .function(args -> {
        String location = (String) args.get("location");
        return "Sunny, 22°C in " + location;
    })
    .build();

// Register and use
ToolExecutor executor = new DefaultToolExecutor();
executor.register(weatherTool);

ChatInput input = ChatInput.builder()
    .message("What's the weather in Paris?")
    .tool(weatherTool)
    .build();

chatModel.call(input).subscribe(System.out::println);
```

### Configuration

```java
ChatModel model = OpenAIChatModel.builder()
    .apiKey(apiKey)
    .modelName("gpt-4")
    .retryStrategy(ExponentialBackoffRetryStrategy.builder()
        .maxAttempts(3)
        .initialDelay(Duration.ofSeconds(1))
        .build())
    .rateLimiter(new TokenBucketRateLimiter(10, 10))
    .interceptor(new LoggingInterceptor())
    .cache(new TieredCache(l1Cache, l2Cache))
    .costTracker(new InMemoryCostTracker())
    .build();
```

## Architecture

```
┌─────────────────────────────────────────┐
│         Application Layer               │
│  (Fluent API / DSL / Domain Logic)     │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│         Framework Core Layer            │
│  (ReactiveModel, ChatModel, etc.)      │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│      Reactive Engine Layer              │
│  (Reactor, Cache, Interceptors)        │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│       Capability Layer                  │
│  (Chat, Embedding, Image, RAG, Tools)  │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│      Infrastructure Layer               │
│  (HTTP Client, Retry, Metrics, Cost)   │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│        Adapter Layer                    │
│  (OpenAI, Azure, Anthropic, Local)     │
└─────────────────────────────────────────┘
```

## Core Concepts

### Reactive Model

All I/O operations return `Mono` or `Flux` for non-blocking async execution:

```java
public interface ReactiveModel<I extends ModelInput, O extends ModelOutput> {
    Mono<O> call(I input);
    Flux<StreamingChunk<O>> stream(I input);
    Flux<O> callBatch(Publisher<I> inputs);
}
```

### Immutable Data

All data structures use Java Records and are immutable:

```java
public record ChatInput(
    List<Message> messages,
    ModelOptions options,
    List<Tool> tools,
    // ...
) implements ModelInput {
    // Defensive copying in compact constructor
}
```

### Builder Pattern

Complex objects use fluent builders:

```java
ChatInput input = ChatInput.builder()
    .system("You are a helpful assistant")
    .user("Tell me about Java 21")
    .options(opts -> opts
        .temperature(0.7)
        .maxTokens(500))
    .build();
```

## Project Structure

```
src/main/java/com/llmframework/
├── core/
│   ├── model/         # Core interfaces
│   ├── message/       # Message types
│   └── usage/         # Token tracking
├── chat/              # Chat models
├── embedding/         # Embedding models
├── image/             # Image models
├── tool/              # Tool/function calling
├── format/            # Response formats
├── cache/             # Caching layer
├── cost/              # Cost tracking
├── exception/         # Exception hierarchy
├── retry/             # Retry strategies
├── ratelimit/         # Rate limiting
├── client/            # HTTP client
├── adapter/           # Provider adapters
└── util/              # Utilities
```

## Building

```bash
mvn clean install
```

## Testing

```bash
mvn test
```

## License

Apache License 2.0

## Contributing

Contributions are welcome! Please read our contributing guidelines first.

## Roadmap

- [ ] Additional model providers (Azure, Anthropic, Local models)
- [ ] RAG pipeline implementation
- [ ] Conversation memory management
- [ ] Prompt template engine
- [ ] Semantic caching
- [ ] Spring Boot integration
- [ ] Comprehensive documentation
- [ ] Example applications

## Version

Current version: 1.0.0-SNAPSHOT
