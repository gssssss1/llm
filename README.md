# Java LangGraph

A Java-native implementation of LangGraph for stateful workflow orchestration, leveraging Java 21 features for modern, high-performance execution.

## 🎉 New: Web UI with Dynamic Execution Graph!

Now includes a **Spring Boot Web UI** with **real-time dynamic execution visualization**!

🌐 **[Web UI Guide](WEB_UI_GUIDE.md)** - Complete documentation for the Web interface  
🎬 **[Dynamic Graph Guide](DYNAMIC_GRAPH_GUIDE.md)** - Learn about the dynamic execution visualization

**Key Features:**
- 📊 **Dynamic Execution Graph** - Watch nodes execute in real-time
- 🎨 **Status Colors** - Visual feedback for running, completed, and failed nodes
- ⚡ **Live Updates** - WebSocket-powered real-time updates
- 📈 **Multiple Views** - Static Mermaid diagrams and dynamic execution flow

**Quick Start Web UI:**
```bash
mvn spring-boot:run -Dstart-class=com.langgraph.web.WebUIDemo
# Then open http://localhost:8080
```

## Features

- **Type-Safe State Management**: Generic bounded types with sealed interfaces
- **Flexible Node Definitions**: Functional interfaces for easy node implementation
- **Multiple Edge Types**: Static, conditional, and parallel edge routing
- **Virtual Threads**: Leverages Java 21 virtual threads for high-concurrency
- **Checkpointing**: Built-in support for execution checkpointing and resumption
- **Interruption Support**: Pause and resume workflow execution
- **Graph Visualization**: Multiple output formats (ASCII, Mermaid, DOT)
- **Plugin System**: Extensible with custom plugins
- **Immutable State**: All state is immutable, preventing shared state mutations
- **🆕 Web UI**: Real-time graph visualization and execution monitoring
- **🆕 Dynamic Execution Graph**: Watch workflow execution in real-time with animated nodes

## Requirements

- Java 21 or higher
- Maven 3.8+

## Quick Start

### 1. Add Dependency

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.langgraph</groupId>
    <artifactId>java-langgraph</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Create a Simple Workflow

```java
import com.langgraph.core.StateGraph;
import com.langgraph.execution.GraphExecutor;
import com.langgraph.node.NodeResult;
import com.langgraph.state.StateRecord;

public class Example {
    public static void main(String[] args) throws Exception {
        // Build a graph
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("start", state -> {
                System.out.println("Starting workflow...");
                return new NodeResult<>(state.withData("step", "started"), "process");
            })
            .addNode("process", state -> {
                System.out.println("Processing...");
                return new NodeResult<>(state.withData("step", "processed"), "end");
            })
            .addNode("end", state -> {
                System.out.println("Complete!");
                return new NodeResult<>(state.withData("step", "completed"));
            })
            .setStartNode("start")
            .setEndNode("end")
            .build();
        
        // Execute
        GraphExecutor<StateRecord> executor = new GraphExecutor<>();
        var result = executor.execute(graph, new StateRecord(), ExecutionConfig.DEFAULT).get();
        
        System.out.println("Final state: " + result.finalState());
        executor.shutdown();
    }
}
```

### 3. Use Conditional Edges

```java
StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
    .addNode("analyze", state -> 
        new NodeResult<>(state.withData("value", 42)))
    .addNode("high_value", state -> 
        new NodeResult<>(state.withData("result", "high")))
    .addNode("low_value", state -> 
        new NodeResult<>(state.withData("result", "low")))
    .setStartNode("analyze")
    .addConditionalEdge("analyze", state -> {
        Integer value = state.get("value", Integer.class);
        return value > 30 ? "high_value" : "low_value";
    })
    .build();
```

### 4. Visualize Graphs

```java
import com.langgraph.viewer.GraphViewer;
import com.langgraph.viewer.GraphExecutionViewer;

// ASCII visualization
System.out.println(GraphViewer.toAscii(graph));

// Mermaid diagram
System.out.println(GraphViewer.toMermaid(graph));

// DOT format
System.out.println(GraphViewer.toDot(graph));

// Execute with visualization
GraphExecutionViewer<StateRecord> viewer = new GraphExecutionViewer<>(graph, executor);
var result = viewer.executeAndDisplay(initialState).get();
```

## Architecture

### Core Components

- **StateGraph<T>**: The main graph structure containing nodes and edges
- **State**: Immutable state interface with `StateRecord` implementation
- **Node<T>**: Represents a computation unit in the graph
- **Edge<T>**: Sealed interface for different edge types (Static, Conditional, Parallel)
- **GraphExecutor<T>**: Execution engine using virtual threads
- **Checkpoint<T>**: State snapshot for resumption

### State Management

All state is immutable. Use the fluent API to create new state instances:

```java
StateRecord state = new StateRecord()
    .withData("key1", "value1")
    .withData("key2", 42)
    .withAllData(Map.of("key3", "value3", "key4", true));
```

### Edge Types

#### Static Edge
Direct connection between two nodes:
```java
.addEdge("nodeA", "nodeB")
```

#### Conditional Edge
Route based on state:
```java
.addConditionalEdge("nodeA", state -> {
    return condition ? "nodeB" : "nodeC";
})
```

#### Parallel Edge
Execute multiple nodes in parallel:
```java
.addParallelEdge("nodeA", "nodeB", "nodeC", "nodeD")
```

## Advanced Features

### Checkpointing

```java
CheckpointStorage<StateRecord> storage = new InMemoryCheckpointStorage<>();
GraphExecutor<StateRecord> executor = new GraphExecutor<>(storage);

// Execution with checkpointing
var result = executor.execute(graph, initialState, 
    new ExecutionConfig(true, true, Duration.ofMinutes(30), 1000)
).get();

// Resume from checkpoint
var resumedResult = executor.resume(graph, executionId, ExecutionConfig.DEFAULT).get();
```

### Custom Plugins

```java
public class LoggingPlugin implements GraphPlugin<StateRecord> {
    @Override
    public void beforeExecution(ExecutionContext<StateRecord> context) {
        System.out.println("Starting execution: " + context.getExecutionId());
    }
    
    @Override
    public void afterExecution(ExecutionContext<StateRecord> context) {
        System.out.println("Finished execution with " + 
            context.getExecutedNodes().size() + " nodes");
    }
}

GraphExecutor<StateRecord> executor = new GraphExecutor<>(
    storage, 
    List.of(new LoggingPlugin())
);
```

### Node Configuration

```java
import com.langgraph.node.NodeConfig;

NodeConfig config = new NodeConfig(
    3,                        // max retries
    Duration.ofSeconds(30),   // timeout
    true                      // retry on exception
);

Node<StateRecord> node = new Node<>("myNode", myFunction, config);
```

## Examples

See the `com.langgraph.examples` package for complete examples:

- `SimpleWorkflowExample`: Basic linear workflow
- `ConditionalWorkflowExample`: Conditional routing based on state

## Building

```bash
mvn clean install
```

## Running Tests

```bash
mvn test
```

## Running Examples

```bash
mvn exec:java -Dexec.mainClass="com.langgraph.examples.SimpleWorkflowExample"
mvn exec:java -Dexec.mainClass="com.langgraph.examples.ConditionalWorkflowExample"
```

## Design Principles

1. **Immutability**: All state is immutable to prevent concurrency issues
2. **Type Safety**: Generic bounds and sealed interfaces ensure compile-time safety
3. **Performance**: Virtual threads enable high-concurrency with low overhead
4. **Extensibility**: Plugin system and interface-based design for customization
5. **Observability**: Built-in logging, metrics, and visualization support

## Comparison with Python LangGraph

| Feature | Python LangGraph | Java LangGraph |
|---------|-----------------|----------------|
| Type Safety | Runtime (with type hints) | Compile-time |
| Concurrency | asyncio/threads | Virtual Threads |
| State | Dict-based | Immutable Records |
| Performance | Good | Excellent |
| Ecosystem | Rich (LangChain) | Growing (Spring AI) |

## Future Roadmap

- [ ] Spring Boot auto-configuration
- [ ] Micrometer metrics integration
- [ ] Distributed execution support
- [ ] Visual graph editor
- [ ] More checkpoint storage backends (Redis, PostgreSQL)
- [ ] Workflow versioning
- [ ] A/B testing support

## License

This project is provided as-is for demonstration purposes.

## Contributing

Contributions are welcome! Please ensure:
- All tests pass
- Code follows existing style conventions
- New features include tests and documentation
