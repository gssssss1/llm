# Contributing to Modern Java LLM Framework

Thank you for your interest in contributing! This document provides guidelines and instructions for contributing to the project.

## Code of Conduct

Be respectful, inclusive, and professional in all interactions.

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.8+
- Git
- An IDE (IntelliJ IDEA, Eclipse, or VS Code recommended)

### Setting Up Development Environment

1. Fork the repository
2. Clone your fork:
   ```bash
   git clone https://github.com/your-username/modern-java-llm-framework.git
   cd modern-java-llm-framework
   ```

3. Build the project:
   ```bash
   mvn clean install
   ```

4. Run tests:
   ```bash
   mvn test
   ```

## Development Guidelines

### Code Style

- Follow standard Java naming conventions
- Use meaningful variable and method names
- Keep methods focused and concise
- Add Javadoc for public APIs
- Use Java 21 features where appropriate (Records, Sealed Classes, Pattern Matching)

### Design Principles

1. **Immutability**: Use Records and immutable collections
2. **Type Safety**: Leverage sealed interfaces and generics
3. **Reactive**: Return Mono/Flux for async operations
4. **Functional**: Use functional programming patterns
5. **Clean Code**: Follow SOLID principles

### Example Code Style

```java
/**
 * Represents a chat message with role and content.
 * 
 * @param role the message role
 * @param content the message content
 */
public record TextMessage(
    MessageRole role,
    String content,
    String name
) implements Message {
    
    @Override
    public int estimateTokens() {
        return TokenCounter.estimate(content);
    }
}
```

### Testing

- Write unit tests for all new features
- Aim for >80% code coverage
- Use descriptive test names
- Follow AAA pattern (Arrange, Act, Assert)

Example test:

```java
@Test
void testChatInputBuilder() {
    // Arrange
    String systemPrompt = "You are helpful";
    String userMessage = "What is AI?";
    
    // Act
    ChatInput input = ChatInput.builder()
        .system(systemPrompt)
        .user(userMessage)
        .build();
    
    // Assert
    assertEquals(2, input.messages().size());
    assertEquals(systemPrompt, input.messages().get(0).content());
    assertEquals(userMessage, input.messages().get(1).content());
}
```

## Contribution Process

### 1. Create an Issue

Before starting work, create an issue describing:
- The problem you're solving
- Your proposed solution
- Any breaking changes

### 2. Create a Branch

```bash
git checkout -b feature/your-feature-name
# or
git checkout -b fix/your-bug-fix
```

Branch naming conventions:
- `feature/` for new features
- `fix/` for bug fixes
- `docs/` for documentation
- `refactor/` for refactoring
- `test/` for test improvements

### 3. Make Changes

- Write clean, well-documented code
- Add tests for new functionality
- Update documentation as needed
- Ensure all tests pass

### 4. Commit Changes

Write clear commit messages following this format:

```
<type>: <subject>

<body>

<footer>
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding/updating tests
- `chore`: Maintenance tasks

Example:

```
feat: Add semantic caching support

Implement vector-based semantic caching that finds similar
queries and returns cached responses when similarity exceeds
the configured threshold.

Closes #123
```

### 5. Push and Create Pull Request

```bash
git push origin feature/your-feature-name
```

Then create a Pull Request on GitHub with:
- Clear title and description
- Reference to related issues
- Screenshots/examples if applicable
- Checklist of changes

### Pull Request Checklist

- [ ] Code follows project style guidelines
- [ ] Tests added/updated and passing
- [ ] Documentation updated
- [ ] No breaking changes (or clearly documented)
- [ ] Commit messages are clear
- [ ] Branch is up to date with main

## Project Structure

```
src/main/java/com/llmframework/
├── core/           # Core abstractions
├── chat/           # Chat model implementation
├── embedding/      # Embedding model implementation
├── image/          # Image model implementation
├── tool/           # Tool/function calling
├── cache/          # Caching layer
├── cost/           # Cost tracking
├── exception/      # Exception hierarchy
├── retry/          # Retry strategies
├── ratelimit/      # Rate limiting
├── client/         # HTTP client
├── adapter/        # Provider adapters
└── util/           # Utilities
```

## Adding a New Feature

### Example: Adding a New Model Provider

1. Create adapter package:
   ```
   src/main/java/com/llmframework/adapter/yourprovider/
   ```

2. Implement model interface:
   ```java
   public class YourProviderChatModel extends AbstractModelClient<ChatInput, ChatOutput>
       implements ChatModel {
       // Implementation
   }
   ```

3. Add tests:
   ```
   src/test/java/com/llmframework/adapter/yourprovider/
   ```

4. Update documentation:
   - README.md
   - ARCHITECTURE.md
   - Add usage examples

## Documentation

### Javadoc

- Add Javadoc for all public APIs
- Include `@param` and `@return` tags
- Provide usage examples in class-level docs

### README Updates

When adding features, update:
- Feature list
- Quick Start examples
- API examples

### Architecture Documentation

For significant changes, update ARCHITECTURE.md:
- Design decisions
- New patterns
- Architectural changes

## Code Review Process

### As a Contributor

- Respond to feedback promptly
- Be open to suggestions
- Make requested changes
- Ask questions if unclear

### As a Reviewer

- Be constructive and respectful
- Explain reasoning for requested changes
- Approve when requirements are met
- Focus on code quality and maintainability

## Release Process

1. Version bump in pom.xml
2. Update CHANGELOG.md
3. Tag release
4. Build and deploy artifacts
5. Publish release notes

## Getting Help

- GitHub Issues for bugs and features
- Discussions for questions
- Discord/Slack for real-time help

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.

## Recognition

Contributors will be:
- Listed in CONTRIBUTORS.md
- Mentioned in release notes
- Credited in documentation

Thank you for contributing to Modern Java LLM Framework!
