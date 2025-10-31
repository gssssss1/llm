# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0-SNAPSHOT] - 2024-01-01

### Added

#### Core Features
- **State Management**
  - `State` sealed interface for type-safe state handling
  - `StateRecord` implementation with immutable data storage
  - Fluent API for state updates (`withData`, `withAllData`)
  - Type-safe data retrieval with `get(key, type)`

- **Graph Structure**
  - `StateGraph<T>` with generic type parameter for state
  - Builder pattern for fluent graph construction
  - Support for start and end nodes
  - Graph validation at build time

- **Node System**
  - `Node<T>` record for immutable node definitions
  - `NodeFunction<T>` functional interface for node implementations
  - `NodeResult<T>` for encapsulating execution results
  - `NodeConfig` for retry logic and timeout configuration

- **Edge Types**
  - `StaticEdge<T>` for direct node connections
  - `ConditionalEdge<T>` for state-based routing
  - `ParallelEdge<T>` for concurrent node execution
  - Sealed `Edge<T>` hierarchy for type safety

- **Execution Engine**
  - `GraphExecutor<T>` with virtual thread support (Java 21)
  - `ExecutionContext<T>` for tracking execution state
  - `ExecutionResult<T>` with status, timing, and error information
  - `ExecutionConfig` for configuring execution behavior
  - Support for parallel node execution
  - Max steps limit to prevent infinite loops
  - Execution timeout support

- **Checkpoint System**
  - `Checkpoint<T>` for workflow state snapshots
  - `CheckpointStorage<T>` interface for pluggable storage
  - `InMemoryCheckpointStorage<T>` implementation
  - Resume capability from checkpoints
  - Checkpoint metadata support

- **Plugin System**
  - `GraphPlugin<T>` interface for extensibility
  - `beforeExecution` and `afterExecution` hooks
  - Support for custom logging, metrics, and monitoring

- **Visualization**
  - `GraphViewer` for multiple output formats
  - ASCII text visualization
  - Mermaid diagram generation
  - DOT format for GraphViz
  - `GraphStats` for graph statistics
  - `GraphExecutionViewer<T>` for integrated execution and display

- **Utilities**
  - `GraphUtils` for graph analysis
  - Cycle detection
  - Topological sorting
  - Reachable node calculation
  - Graph depth calculation
  - Node depth mapping

#### Examples
- `SimpleWorkflowExample` - Basic linear workflow
- `ConditionalWorkflowExample` - Conditional routing demonstration
- `ParallelWorkflowExample` - Parallel execution demonstration

#### Documentation
- Comprehensive README.md with quick start guide
- Detailed ARCHITECTURE.md documentation
- CONTRIBUTING.md with development guidelines
- MIT LICENSE
- Inline Javadoc for public APIs
- Logback configuration for structured logging

#### Testing
- Unit tests for state management
- Unit tests for graph construction and validation
- Integration tests for graph execution
- Checkpoint storage tests
- Example verification tests

### Technical Details

#### Java 21 Features
- Virtual threads for high-concurrency execution
- Record classes for immutable data structures
- Sealed interfaces for controlled type hierarchies
- Pattern matching for type discrimination
- Text blocks for multi-line string generation

#### Dependencies
- Jackson 2.17.0 for JSON serialization
- SLF4J 2.0.12 + Logback 1.5.3 for logging
- JUnit 5.10.2 for testing

#### Build System
- Maven 3.8+ with Java 21 configuration
- Compiler plugin with preview features enabled
- Surefire plugin for test execution

### Design Decisions

#### Immutability
- All state objects are immutable
- Copy-on-write semantics for state updates
- Thread-safe by design

#### Type Safety
- Generic bounded types (`T extends State`)
- Sealed interfaces prevent uncontrolled extension
- Compile-time type checking

#### Concurrency
- Virtual threads for lightweight concurrency
- No shared mutable state between nodes
- Atomic operations for interruption flags

#### Performance
- Lazy checkpoint serialization
- Concurrent collections for storage
- Virtual thread pooling by JVM

### Known Limitations

- In-memory checkpoint storage is not persistent across restarts
- Conditional edges don't provide compile-time validation of targets
- No distributed execution support yet
- No Spring Boot auto-configuration yet

### Future Roadmap

- [ ] Redis-based checkpoint storage
- [ ] PostgreSQL-based checkpoint storage
- [ ] Spring Boot starter and auto-configuration
- [ ] Micrometer metrics integration
- [ ] OpenTelemetry tracing support
- [ ] Visual graph editor (web-based)
- [ ] Workflow versioning and A/B testing
- [ ] Distributed execution with Kubernetes operator
- [ ] Spring AI integration
- [ ] Enhanced retry policies with exponential backoff
- [ ] Graph composition (sub-graphs)
- [ ] Dynamic graph modification
- [ ] Workflow monitoring dashboard

## Version History

### Version Numbering

- **MAJOR**: Incompatible API changes
- **MINOR**: New features, backward compatible
- **PATCH**: Bug fixes, backward compatible

### Release Notes Template

Each release includes:
- New features and enhancements
- Bug fixes
- Breaking changes
- Deprecations
- Migration guides (for breaking changes)
- Performance improvements
- Documentation updates

---

[Unreleased]: https://github.com/username/java-langgraph/compare/v1.0.0...HEAD
[1.0.0-SNAPSHOT]: https://github.com/username/java-langgraph/releases/tag/v1.0.0
