# Contributing to Java LangGraph

Thank you for your interest in contributing to Java LangGraph! This document provides guidelines and instructions for contributing.

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.8+
- Git
- A Java IDE (IntelliJ IDEA, Eclipse, or VS Code with Java extensions)

### Setting Up Development Environment

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd java-langgraph
   ```

2. **Build the project**:
   ```bash
   mvn clean install
   ```

3. **Run tests**:
   ```bash
   mvn test
   ```

4. **Run examples**:
   ```bash
   mvn exec:java -Dexec.mainClass="com.langgraph.examples.SimpleWorkflowExample"
   ```

## Code Style Guidelines

### General Principles

1. **Follow existing code style**: Match the style of the codebase
2. **Use Java 21 features**: Leverage records, sealed interfaces, virtual threads
3. **Prefer immutability**: Use immutable data structures
4. **Type safety first**: Use generics and bounded types
5. **Minimize comments**: Write self-documenting code; comment only complex logic

### Naming Conventions

- **Classes**: PascalCase (e.g., `StateGraph`, `NodeFunction`)
- **Interfaces**: PascalCase, no 'I' prefix (e.g., `State`, not `IState`)
- **Methods**: camelCase (e.g., `executeNode`, `getNextNodes`)
- **Variables**: camelCase (e.g., `currentState`, `executionId`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `DEFAULT_CONFIG`, `MAX_RETRIES`)
- **Packages**: lowercase (e.g., `com.langgraph.execution`)

### Code Structure

#### Use Records for Immutable Data
```java
// Good
public record NodeConfig(int maxRetries, Duration timeout, boolean retryOnException) {
    // Compact constructor for validation
    public NodeConfig {
        if (maxRetries < 0) {
            throw new IllegalArgumentException("Max retries cannot be negative");
        }
    }
}

// Avoid
public class NodeConfig {
    private final int maxRetries;
    private final Duration timeout;
    // ... lots of boilerplate
}
```

#### Use Sealed Interfaces for Controlled Hierarchies
```java
// Good
public sealed interface Edge<T extends State> 
    permits StaticEdge, ConditionalEdge, ParallelEdge {
    String from();
    String to();
}

// Avoid - allows uncontrolled extension
public interface Edge<T extends State> {
    String from();
    String to();
}
```

#### Use Pattern Matching
```java
// Good
if (edge instanceof StaticEdge<T> staticEdge) {
    processStaticEdge(staticEdge);
} else if (edge instanceof ConditionalEdge<T> conditionalEdge) {
    processConditionalEdge(conditionalEdge);
}

// Avoid
if (edge instanceof StaticEdge) {
    StaticEdge<T> staticEdge = (StaticEdge<T>) edge;
    processStaticEdge(staticEdge);
}
```

### Documentation

#### Javadoc Requirements

Document all public APIs:

```java
/**
 * Executes a state graph workflow from start to completion.
 * 
 * <p>This method runs the graph asynchronously using virtual threads,
 * allowing for high-concurrency execution without blocking platform threads.
 * 
 * @param graph the state graph to execute
 * @param initialState the initial state to start execution with
 * @param config execution configuration including timeout and checkpointing settings
 * @param <T> the state type, must extend {@link State}
 * @return a CompletableFuture containing the execution result
 * @throws IllegalArgumentException if graph or initialState is null
 */
public <T extends State> CompletableFuture<ExecutionResult<T>> execute(
    StateGraph<T> graph,
    T initialState,
    ExecutionConfig config
) {
    // Implementation
}
```

## Testing Guidelines

### Test Structure

```java
class StateGraphTest {
    
    @BeforeEach
    void setUp() {
        // Initialize test fixtures
    }
    
    @Test
    void testMethodName_Scenario_ExpectedBehavior() {
        // Arrange
        StateGraph<StateRecord> graph = createTestGraph();
        
        // Act
        var result = graph.getNextNodes("start", state);
        
        // Assert
        assertEquals(1, result.size());
        assertEquals("nextNode", result.get(0));
    }
    
    @AfterEach
    void tearDown() {
        // Cleanup
    }
}
```

### Test Coverage Requirements

- **New features**: Must include tests
- **Bug fixes**: Must include regression tests
- **Target coverage**: Aim for >80% line coverage
- **Critical paths**: 100% coverage for core execution logic

### Test Categories

1. **Unit Tests**: Test individual components in isolation
2. **Integration Tests**: Test component interactions
3. **Performance Tests**: Benchmark critical operations
4. **Example Tests**: Ensure examples work correctly

## Pull Request Process

### Before Submitting

1. **Run all tests**: `mvn test`
2. **Check code style**: Ensure consistency
3. **Update documentation**: README, ARCHITECTURE, Javadoc
4. **Add tests**: For new features or bug fixes
5. **Update CHANGELOG**: Document your changes

### PR Description Template

```markdown
## Description
Brief description of the changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
Describe the tests you've added or run

## Checklist
- [ ] My code follows the style guidelines
- [ ] I have performed a self-review
- [ ] I have commented complex code
- [ ] I have updated documentation
- [ ] My changes generate no new warnings
- [ ] I have added tests
- [ ] All tests pass locally
```

### Review Process

1. Submit PR with clear description
2. Address reviewer comments
3. Ensure CI passes (when available)
4. Obtain approval from maintainer
5. Squash commits if requested

## Feature Development Workflow

### 1. Design Phase

Before implementing major features:

1. Create an issue describing the feature
2. Discuss design approach
3. Get approval from maintainers
4. Document design decisions

### 2. Implementation Phase

1. Create a feature branch: `feature/your-feature-name`
2. Implement incrementally with commits
3. Write tests alongside implementation
4. Update documentation

### 3. Review Phase

1. Submit PR
2. Respond to feedback
3. Iterate until approved
4. Merge to main

## Architecture Decisions

### When Adding New Components

Consider these questions:

1. **Is it immutable?** Prefer immutable designs
2. **Is it thread-safe?** Document concurrency guarantees
3. **Does it fit the pattern?** Follow existing architectural patterns
4. **Is it testable?** Write tests during development
5. **Is it extensible?** Consider future enhancements

### When Modifying Existing Components

1. **Check backward compatibility**: Avoid breaking changes
2. **Update related tests**: Ensure existing tests still pass
3. **Consider deprecation**: For major API changes
4. **Document migration**: If breaking changes are necessary

## Reporting Issues

### Bug Reports

Include:

1. **Description**: Clear description of the bug
2. **Steps to reproduce**: Minimal reproduction steps
3. **Expected behavior**: What should happen
4. **Actual behavior**: What actually happens
5. **Environment**: Java version, OS, etc.
6. **Stack trace**: If applicable

### Feature Requests

Include:

1. **Use case**: Why is this feature needed?
2. **Proposed solution**: How should it work?
3. **Alternatives**: Other approaches considered
4. **Additional context**: Examples, mockups, etc.

## Communication

### Channels

- **GitHub Issues**: Bug reports, feature requests
- **GitHub Discussions**: Questions, ideas, general discussion
- **Pull Requests**: Code review and feedback

### Response Times

- We aim to respond to issues within 2-3 business days
- Complex discussions may take longer
- PRs typically reviewed within 1 week

## Code of Conduct

### Our Standards

- Be respectful and inclusive
- Welcome diverse perspectives
- Focus on constructive criticism
- Assume good intentions
- Help others learn and grow

### Unacceptable Behavior

- Harassment or discrimination
- Trolling or insulting comments
- Personal or political attacks
- Publishing others' private information
- Other conduct considered inappropriate

## Recognition

Contributors will be:

- Listed in CONTRIBUTORS.md
- Credited in release notes
- Acknowledged in documentation

## Questions?

If you have questions about contributing:

1. Check existing documentation
2. Search issues and discussions
3. Create a new discussion
4. Reach out to maintainers

Thank you for contributing to Java LangGraph!
