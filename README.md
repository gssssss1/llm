# LoomEvent-Engine

LoomEvent-Engine is a framework designed to leverage Java 21 virtual threads for high-throughput event processing.

## Prerequisites

- Java 21
- Gradle (Wrapper is provided)

## Building the Project

To build the project, run:

```bash
./gradlew build
```

## Running Tests

To run the unit tests:

```bash
./gradlew test
```

## Quality Gates

This project uses the following quality gates:

- **Checkstyle**: Enforces code style guidelines.
- **Spotless**: Enforces code formatting.

To format the code:

```bash
./gradlew spotlessApply
```

To run checks:

```bash
./gradlew check
```

## Project Structure

- `io.cto.loomevent.core`: Core classes.
- `io.cto.loomevent.core.util`: Shared utilities (e.g., lock-free helpers).
