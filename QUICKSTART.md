# Quick Start Guide

Get started with Java LangGraph in 5 minutes!

## Prerequisites

- Java 21 or higher
- Maven 3.8+

## Installation

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.langgraph</groupId>
    <artifactId>java-langgraph</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Or clone and build locally:

```bash
git clone <repository-url>
cd java-langgraph
mvn clean install
```

## Your First Workflow

### Step 1: Create a Simple Graph

```java
import com.langgraph.core.StateGraph;
import com.langgraph.execution.GraphExecutor;
import com.langgraph.node.NodeResult;
import com.langgraph.state.StateRecord;

public class HelloWorld {
    public static void main(String[] args) throws Exception {
        // Build a simple 3-node workflow
        StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
            .addNode("greet", state -> {
                System.out.println("Hello from the workflow!");
                return new NodeResult<>(state.withData("greeted", true), "process");
            })
            .addNode("process", state -> {
                System.out.println("Processing...");
                return new NodeResult<>(state.withData("processed", true), "finish");
            })
            .addNode("finish", state -> {
                System.out.println("Done!");
                return new NodeResult<>(state.withData("status", "complete"));
            })
            .setStartNode("greet")
            .setEndNode("finish")
            .build();
        
        // Execute the workflow
        GraphExecutor<StateRecord> executor = new GraphExecutor<>();
        var result = executor.execute(graph, new StateRecord(), ExecutionConfig.DEFAULT).get();
        
        System.out.println("Workflow completed: " + result.isSuccessful());
        executor.shutdown();
    }
}
```

**Output:**
```
Hello from the workflow!
Processing...
Done!
Workflow completed: true
```

### Step 2: Add State Management

```java
// Create initial state with data
Map<String, Object> data = new HashMap<>();
data.put("counter", 0);
data.put("name", "Alice");
StateRecord initialState = new StateRecord(data);

// Node that reads and updates state
.addNode("increment", state -> {
    int counter = state.get("counter", Integer.class);
    String name = state.get("name", String.class);
    
    System.out.println(name + ", counter is: " + counter);
    
    return new NodeResult<>(
        state.withData("counter", counter + 1)
    );
})
```

### Step 3: Add Conditional Routing

```java
StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
    .addNode("start", state -> 
        new NodeResult<>(state.withData("value", 42)))
    .addNode("high", state -> 
        new NodeResult<>(state.withData("result", "High value!")))
    .addNode("low", state -> 
        new NodeResult<>(state.withData("result", "Low value!")))
    .setStartNode("start")
    .addConditionalEdge("start", state -> {
        Integer value = state.get("value", Integer.class);
        return value > 30 ? "high" : "low";
    })
    .build();
```

### Step 4: Visualize Your Graph

```java
import com.langgraph.viewer.GraphViewer;

// Print ASCII visualization
System.out.println(GraphViewer.toAscii(graph));

// Generate Mermaid diagram
System.out.println(GraphViewer.toMermaid(graph));

// Get statistics
System.out.println(GraphViewer.getStats(graph));
```

**Output:**
```
Graph Structure:
================

Start Node: start
End Node: finish

Nodes:
  - start
  - high
  - low
  - finish

Edges:
  start -> [conditional]
  high -> finish
  low -> finish

Graph Statistics:
  Nodes: 4
  Edges: 3
    - Static: 2
    - Conditional: 1
    - Parallel: 0
```

### Step 5: Use the Execution Viewer

```java
import com.langgraph.viewer.GraphExecutionViewer;

GraphExecutor<StateRecord> executor = new GraphExecutor<>();
GraphExecutionViewer<StateRecord> viewer = new GraphExecutionViewer<>(graph, executor);

// Execute with visualization
var result = viewer.executeAndDisplay(initialState).get();
```

This shows the graph structure, execution progress, and final results!

## Common Patterns

### Pattern 1: Linear Workflow

```java
StateGraph<StateRecord> workflow = StateGraph.<StateRecord>builder()
    .addNode("step1", state -> new NodeResult<>(state, "step2"))
    .addNode("step2", state -> new NodeResult<>(state, "step3"))
    .addNode("step3", state -> new NodeResult<>(state))
    .setStartNode("step1")
    .build();
```

### Pattern 2: Decision Tree

```java
StateGraph<StateRecord> decision = StateGraph.<StateRecord>builder()
    .addNode("analyze", state -> new NodeResult<>(state))
    .addNode("pathA", state -> new NodeResult<>(state))
    .addNode("pathB", state -> new NodeResult<>(state))
    .addNode("pathC", state -> new NodeResult<>(state))
    .setStartNode("analyze")
    .addConditionalEdge("analyze", state -> {
        String type = state.get("type", String.class);
        return switch (type) {
            case "A" -> "pathA";
            case "B" -> "pathB";
            default -> "pathC";
        };
    })
    .build();
```

### Pattern 3: Parallel Processing

```java
StateGraph<StateRecord> parallel = StateGraph.<StateRecord>builder()
    .addNode("prepare", state -> new NodeResult<>(state))
    .addNode("task1", state -> new NodeResult<>(state, "merge"))
    .addNode("task2", state -> new NodeResult<>(state, "merge"))
    .addNode("task3", state -> new NodeResult<>(state, "merge"))
    .addNode("merge", state -> new NodeResult<>(state))
    .setStartNode("prepare")
    .setEndNode("merge")
    .addParallelEdge("prepare", "task1", "task2", "task3")
    .build();
```

### Pattern 4: Loop with Exit Condition

```java
StateGraph<StateRecord> loop = StateGraph.<StateRecord>builder()
    .addNode("process", state -> {
        int count = state.get("count", Integer.class);
        return new NodeResult<>(state.withData("count", count + 1));
    })
    .addNode("finish", state -> new NodeResult<>(state))
    .setStartNode("process")
    .setEndNode("finish")
    .addConditionalEdge("process", state -> {
        int count = state.get("count", Integer.class);
        return count < 5 ? "process" : "finish";
    })
    .build();
```

## Advanced Features

### Checkpointing

```java
// Create checkpoint storage
CheckpointStorage<StateRecord> storage = new InMemoryCheckpointStorage<>();
GraphExecutor<StateRecord> executor = new GraphExecutor<>(storage);

// Enable checkpointing
ExecutionConfig config = new ExecutionConfig(
    true,  // enable checkpointing
    true,  // enable parallel execution
    Duration.ofMinutes(5),
    1000
);

var result = executor.execute(graph, initialState, config).get();

// Resume from checkpoint
var resumed = executor.resume(graph, result.executionId(), config).get();
```

### Custom Plugins

```java
class TimingPlugin implements GraphPlugin<StateRecord> {
    private long start;
    
    @Override
    public void beforeExecution(ExecutionContext<StateRecord> context) {
        start = System.currentTimeMillis();
        System.out.println("Starting: " + context.getExecutionId());
    }
    
    @Override
    public void afterExecution(ExecutionContext<StateRecord> context) {
        long duration = System.currentTimeMillis() - start;
        System.out.println("Completed in " + duration + "ms");
    }
}

GraphExecutor<StateRecord> executor = new GraphExecutor<>(
    storage,
    List.of(new TimingPlugin())
);
```

### Error Handling

```java
var result = executor.execute(graph, initialState, config).get();

if (result.hasFailed()) {
    System.err.println("Execution failed!");
    result.error().ifPresent(Throwable::printStackTrace);
} else if (result.isInterrupted()) {
    System.out.println("Execution was interrupted");
} else {
    System.out.println("Success!");
}
```

## Running Examples

The repository includes several examples:

```bash
# Simple workflow
mvn exec:java -Dexec.mainClass="com.langgraph.examples.SimpleWorkflowExample"

# Conditional routing
mvn exec:java -Dexec.mainClass="com.langgraph.examples.ConditionalWorkflowExample"

# Parallel execution
mvn exec:java -Dexec.mainClass="com.langgraph.examples.ParallelWorkflowExample"

# Comprehensive example
mvn exec:java -Dexec.mainClass="com.langgraph.examples.ComprehensiveExample"
```

## Next Steps

- Read the [Architecture Documentation](ARCHITECTURE.md) for deep dive
- Check [Contributing Guidelines](CONTRIBUTING.md) to contribute
- Browse the `src/main/java/com/langgraph/examples` directory for more examples
- Explore the test suite for additional usage patterns

## Need Help?

- Check the [README](README.md) for detailed documentation
- Look at the examples in the `examples` package
- Review the test cases for usage patterns
- Open an issue for questions or bugs

## Quick Reference

### Core Classes

- `StateGraph<T>` - The workflow graph
- `StateRecord` - Immutable state container
- `NodeFunction<T>` - Node implementation
- `NodeResult<T>` - Node execution result
- `GraphExecutor<T>` - Execution engine
- `ExecutionResult<T>` - Execution outcome

### Builder Methods

- `.addNode(name, function)` - Add a node
- `.addEdge(from, to)` - Add static edge
- `.addConditionalEdge(from, condition)` - Add conditional routing
- `.addParallelEdge(from, targets...)` - Add parallel execution
- `.setStartNode(name)` - Set entry point
- `.setEndNode(name)` - Set exit point
- `.build()` - Create the graph

### State Methods

- `.withData(key, value)` - Add/update single value
- `.withAllData(map)` - Add/update multiple values
- `.get(key)` - Get value
- `.get(key, type)` - Get typed value

Happy coding with Java LangGraph! 🚀
