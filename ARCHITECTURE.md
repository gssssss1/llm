# Java LangGraph Architecture Documentation

## Overview

This document provides a detailed architectural overview of the Java LangGraph implementation, a stateful workflow orchestration framework built with Java 21.

## Architecture Layers

```
┌─────────────────────────────────────────────────────────┐
│                    Application Layer                     │
│          (Examples, Custom Workflows, Plugins)           │
└─────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────┐
│                  Visualization Layer                     │
│        (GraphViewer, GraphExecutionViewer, Stats)        │
└─────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────┐
│                   Execution Layer                        │
│    (GraphExecutor, ExecutionContext, ExecutionResult)    │
└─────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────┐
│                     Core Layer                           │
│      (StateGraph, Node, Edge, State, Checkpoint)         │
└─────────────────────────────────────────────────────────┘
```

## Core Components

### 1. State Management

#### State Interface (Sealed)
- **Purpose**: Defines the contract for all state objects
- **Design**: Sealed interface permits only `StateRecord`
- **Benefits**: Pattern matching, type safety, controlled extension

```java
sealed interface State permits StateRecord
```

#### StateRecord
- **Immutability**: All data is copied on creation
- **Uniqueness**: Each state has a unique ID
- **Temporal**: Includes timestamp for tracking
- **Flexible**: Map-based data storage

**Key Methods**:
- `withData(key, value)`: Create new state with additional data
- `withAllData(map)`: Merge multiple data points
- `get(key, type)`: Type-safe data retrieval

### 2. Node System

#### Node<T extends State>
- **Record Type**: Immutable node definition
- **Components**:
  - `name`: Unique identifier
  - `function`: NodeFunction implementation
  - `config`: Node-specific configuration

#### NodeFunction<T>
- **Functional Interface**: Single abstract method pattern
- **Signature**: `NodeResult<T> apply(T currentState) throws Exception`
- **Benefits**: Lambda support, composability

#### NodeResult<T>
- **Purpose**: Encapsulates node execution outcome
- **Contains**:
  - `updatedState`: New state after node execution
  - `nextNodes`: Optional routing override

#### NodeConfig
- **Retry Logic**: Max retries and retry conditions
- **Timeout**: Per-node execution timeout
- **Error Handling**: Exception handling strategy

### 3. Edge System

#### Edge<T> (Sealed Interface)
- **Hierarchy**:
  ```
  Edge<T>
  ├── StaticEdge<T>
  ├── ConditionalEdge<T>
  └── ParallelEdge<T>
  ```

#### StaticEdge
- **Purpose**: Direct, unconditional routing
- **Usage**: Simple sequential workflows
- **Performance**: Zero runtime overhead

#### ConditionalEdge
- **Purpose**: State-based routing decisions
- **Function**: `Function<T, String>` evaluates next node
- **Usage**: Branching logic, decision points

#### ParallelEdge
- **Purpose**: Concurrent execution of multiple nodes
- **Implementation**: Virtual threads for parallelism
- **Usage**: Independent parallel tasks

### 4. StateGraph<T>

#### Builder Pattern
- **Fluent API**: Chainable method calls
- **Validation**: Graph integrity checked at build time
- **Immutability**: Built graph is immutable

#### Validation Rules
1. Start node must be set
2. Start node must exist in graph
3. All edge sources must exist
4. All edge targets must exist (static/parallel)
5. No dangling references

#### Graph Queries
- `getNode(name)`: Retrieve node by name
- `getEdgesFrom(node)`: Get outgoing edges
- `getNextNodes(node, state)`: Evaluate routing

### 5. Execution Engine

#### GraphExecutor<T>

**Concurrency Model**:
- **Virtual Threads**: Java 21's lightweight threads
- **ExecutorService**: `newVirtualThreadPerTaskExecutor()`
- **Benefits**: 
  - Millions of concurrent tasks possible
  - No carrier thread blocking
  - Simple async/await style

**Execution Flow**:
```
1. Create ExecutionContext
2. Fire beforeExecution plugins
3. Execute start node
4. Loop:
   a. Execute current node
   b. Update state (immutably)
   c. Save checkpoint (if enabled)
   d. Evaluate routing
   e. Check interruption
   f. Check max steps
5. Fire afterExecution plugins
6. Return ExecutionResult
```

**Parallel Execution**:
- Detected via ParallelEdge or NodeResult with multiple next nodes
- Each parallel branch executes in separate virtual thread
- `CompletableFuture.allOf()` waits for completion
- Results merged (last result's state used)

#### ExecutionContext<T>
- **Thread Safety**: Atomic operations for interruption
- **Tracking**: Execution ID, executed nodes, step count
- **State Management**: Current state and node tracking
- **MDC Integration**: Structured logging context

#### ExecutionResult<T>
- **Status**: COMPLETED, FAILED, INTERRUPTED, TIMEOUT
- **Metadata**: Execution ID, timing, executed nodes
- **Error Handling**: Optional error information
- **Metrics**: Duration calculation

### 6. Checkpoint System

#### Checkpoint<T>
- **Snapshot**: Complete state at a point in time
- **Metadata**: Execution ID, node, timestamp, custom data
- **Resume Support**: Enables workflow resumption

#### CheckpointStorage<T>
- **Interface**: Pluggable storage backend
- **Operations**:
  - `save(checkpoint)`: Persist checkpoint
  - `load(id)`: Retrieve specific checkpoint
  - `loadByExecution(id)`: Get all checkpoints for execution
  - `loadLatest(id)`: Get most recent checkpoint
  - `delete(id)`: Remove checkpoint
  - `deleteByExecution(id)`: Clean up execution

#### InMemoryCheckpointStorage<T>
- **Implementation**: ConcurrentHashMap for thread safety
- **Use Case**: Development, testing, short-lived executions
- **Limitations**: Not persistent across restarts

### 7. Visualization

#### GraphViewer
- **ASCII Output**: Simple text-based visualization
- **Mermaid**: Interactive diagrams for documentation
- **DOT Format**: GraphViz compatible output
- **Statistics**: Node/edge counts and types

#### GraphExecutionViewer<T>
- **Integrated Execution**: Execute with real-time output
- **Display Options**: Pre-execution graph view
- **Results Presentation**: Formatted execution results

### 8. Plugin System

#### GraphPlugin<T>
- **Lifecycle Hooks**:
  - `beforeExecution(context)`: Pre-execution setup
  - `afterExecution(context)`: Post-execution cleanup
- **Use Cases**:
  - Logging and monitoring
  - Metrics collection
  - State validation
  - Custom checkpointing

## Java 21 Features Utilized

### 1. Virtual Threads (JEP 444)
```java
Executors.newVirtualThreadPerTaskExecutor()
```
- **Benefits**: Lightweight concurrency
- **Use**: Node execution, parallel edges
- **Impact**: High-concurrency workflows

### 2. Record Classes (JEP 395)
```java
public record StateRecord(String id, Instant timestamp, Map<String, Object> data)
```
- **Benefits**: Immutability, concise syntax
- **Use**: State, Node, Edge, Checkpoint, Config
- **Impact**: Less boilerplate, clear intent

### 3. Sealed Interfaces (JEP 409)
```java
sealed interface State permits StateRecord
sealed interface Edge<T> permits StaticEdge, ConditionalEdge, ParallelEdge
```
- **Benefits**: Controlled type hierarchy
- **Use**: State and Edge hierarchies
- **Impact**: Better pattern matching, exhaustiveness

### 4. Pattern Matching (Enhanced)
```java
if (edge instanceof StaticEdge<T> staticEdge) {
    // Use staticEdge directly
}
```
- **Benefits**: Less casting, clearer code
- **Use**: Edge routing, type discrimination

### 5. Text Blocks (JEP 378)
- **Use**: Mermaid/DOT diagram generation
- **Benefits**: Readable multi-line strings

## Design Patterns

### 1. Builder Pattern
- **Where**: StateGraph.Builder
- **Why**: Complex object construction, validation

### 2. Strategy Pattern
- **Where**: Edge routing, NodeFunction
- **Why**: Pluggable behavior

### 3. Template Method
- **Where**: GraphExecutor execution flow
- **Why**: Consistent execution with extension points

### 4. Plugin Pattern
- **Where**: GraphPlugin system
- **Why**: Extensibility without modification

### 5. Immutable Object Pattern
- **Where**: All state, records
- **Why**: Thread safety, predictability

## Concurrency Model

### Thread Safety Guarantees

1. **Immutable State**: No shared mutable state between nodes
2. **Thread Isolation**: Each execution has isolated context
3. **Atomic Operations**: Interruption flags use AtomicBoolean
4. **Concurrent Collections**: CheckpointStorage uses ConcurrentHashMap

### Parallel Execution Model

```
                    ┌─────────┐
                    │ prepare │
                    └────┬────┘
                         │
          ┌──────────────┼──────────────┐
          │              │              │
     ┌────▼────┐    ┌────▼────┐   ┌────▼────┐
     │ task_a  │    │ task_b  │   │ task_c  │
     └────┬────┘    └────┬────┘   └────┬────┘
          │              │              │
          └──────────────┼──────────────┘
                         │
                    ┌────▼────┐
                    │  merge  │
                    └─────────┘
```

**Virtual Thread Scheduling**:
- Each parallel task gets its own virtual thread
- Carrier threads (platform threads) managed by JVM
- Blocking operations don't block carrier threads
- Automatic work-stealing for load balancing

## Error Handling Strategy

### Exception Hierarchy
```
Exception (checked)
├── User-recoverable errors
│   └── Retry with backoff
└── RuntimeException (unchecked)
    └── Fatal errors
        └── Fail execution
```

### Retry Logic
1. Node-level retry configuration
2. Exponential backoff (planned)
3. Max retry limits
4. Error callbacks (via plugins)

### Failure Modes
- **Node Failure**: Captured in ExecutionResult
- **Timeout**: ExecutionStatus.TIMEOUT
- **Interruption**: ExecutionStatus.INTERRUPTED
- **Max Steps**: IllegalStateException

## Performance Considerations

### 1. State Serialization
- **Current**: Jackson JSON serialization
- **Optimization**: Lazy serialization (only when checkpointing)
- **Future**: Binary formats (Kryo, Protobuf)

### 2. Graph Compilation
- **Current**: Runtime edge evaluation
- **Future**: Pre-compile routing tables
- **Benefit**: O(1) routing vs O(n) edge scanning

### 3. Memory Management
- **Checkpoints**: Consider weak references for old checkpoints
- **State**: Copy-on-write for data maps
- **Cleanup**: Automatic expired execution cleanup

### 4. Virtual Thread Overhead
- **Minimal**: ~1KB per virtual thread
- **Scalability**: Can handle millions of concurrent nodes
- **Carrier Threads**: Typically matches CPU cores

## Testing Strategy

### Unit Tests
- **State**: Immutability, data access
- **Graph**: Construction, validation, routing
- **Execution**: Single node, multiple nodes, conditions
- **Checkpoints**: Save, load, delete operations

### Integration Tests
- **End-to-End**: Complete workflow execution
- **Concurrency**: Parallel execution correctness
- **Resume**: Checkpoint and resume flows

### Performance Tests
- **Throughput**: Nodes per second
- **Latency**: End-to-end execution time
- **Scalability**: Large graph execution
- **Memory**: State and checkpoint overhead

## Security Considerations

### 1. State Isolation
- Each execution has isolated state
- No cross-execution data leakage
- Immutability prevents tampering

### 2. Input Validation
- Node name validation
- State data sanitization (application responsibility)
- Graph structure validation at build time

### 3. Resource Limits
- Max steps prevents infinite loops
- Timeout prevents runaway executions
- Checkpoint storage limits (configurable)

### 4. Serialization Security
- Jackson ObjectMapper with type validation
- No arbitrary deserialization
- Controlled polymorphism

## Monitoring & Observability

### Logging
- **SLF4J**: Standard logging facade
- **MDC**: Execution ID in context
- **Levels**:
  - DEBUG: Node execution, routing decisions
  - INFO: Execution start/complete
  - WARN: Retries, near-limits
  - ERROR: Execution failures

### Metrics (Future)
- **Micrometer Integration**:
  - Active executions gauge
  - Node execution timer
  - Graph throughput counter
  - Error rate counter

### Tracing (Future)
- **OpenTelemetry**: Distributed tracing
- **Spans**: Per-node execution
- **Correlation**: Execution ID propagation

## Extension Points

### 1. Custom State Types
```java
public record MyState(String id, Instant timestamp, 
                     Map<String, Object> data,
                     MyCustomData custom) implements State
```

### 2. Custom Edge Types
```java
public record WeightedEdge<T extends State>(
    String from, String to, double weight
) implements Edge<T>
```

### 3. Custom Storage Backends
```java
public class RedisCheckpointStorage<T extends State> 
    implements CheckpointStorage<T> {
    // Redis implementation
}
```

### 4. Custom Plugins
```java
public class MetricsPlugin<T extends State> implements GraphPlugin<T> {
    // Metrics collection
}
```

## Migration from Python LangGraph

### Step 1: Map State Schema
```python
# Python
state = {"key": "value", "count": 0}
```
```java
// Java
StateRecord state = new StateRecord()
    .withData("key", "value")
    .withData("count", 0);
```

### Step 2: Convert Node Functions
```python
# Python
def my_node(state: dict) -> dict:
    state["processed"] = True
    return state
```
```java
// Java
NodeFunction<StateRecord> myNode = state -> 
    new NodeResult<>(state.withData("processed", true));
```

### Step 3: Translate Routing
```python
# Python
def route(state: dict) -> str:
    return "nodeA" if state["value"] > 10 else "nodeB"
```
```java
// Java
Function<StateRecord, String> route = state -> {
    Integer value = state.get("value", Integer.class);
    return value > 10 ? "nodeA" : "nodeB";
};
```

### Step 4: Build Graph
```python
# Python
graph = StateGraph()
graph.add_node("start", start_node)
graph.add_conditional_edges("start", route)
graph.set_entry_point("start")
```
```java
// Java
StateGraph<StateRecord> graph = StateGraph.<StateRecord>builder()
    .addNode("start", startNode)
    .addConditionalEdge("start", route)
    .setStartNode("start")
    .build();
```

## Future Enhancements

### 1. Spring Boot Integration
```java
@SpringBootApplication
@EnableLangGraph
public class MyApp {
    @Bean
    public StateGraph<MyState> workflow() {
        return StateGraph.<MyState>builder()
            .addNode("start", startNode)
            .setStartNode("start")
            .build();
    }
}
```

### 2. Distributed Execution
- Kubernetes operator
- Cluster-based execution
- Distributed checkpointing

### 3. Visual Editor
- Web-based graph builder
- Drag-and-drop nodes
- Live execution monitoring

### 4. Workflow Versioning
- A/B testing support
- Canary deployments
- Rollback capabilities

## Conclusion

The Java LangGraph architecture leverages modern Java 21 features to provide a robust, type-safe, and high-performance workflow orchestration framework. The design prioritizes immutability, thread safety, and extensibility while maintaining simplicity and clarity.
