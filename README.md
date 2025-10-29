# Backend Project

A Spring Boot (Java 21) backend application with a modular architecture for graph processing and execution.

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/project/backend/
│   │   │   ├── api/             # REST API controllers and DTOs
│   │   │   ├── domain/          # Core graph domain models
│   │   │   ├── engine/          # Execution engine for graph processing
│   │   │   ├── persistence/     # Data persistence layer (JPA repositories)
│   │   │   ├── config/          # Spring configuration classes
│   │   │   └── BackendApplication.java
│   │   └── resources/
│   │       └── application.yml  # Application configuration
│   └── test/
│       ├── java/com/project/backend/
│       │   ├── testcontainers/  # Testcontainers configuration
│       │   └── BackendApplicationTests.java
│       └── resources/
├── build.gradle                 # Gradle build configuration
├── settings.gradle
├── gradlew                      # Gradle wrapper (Unix)
└── gradlew.bat                  # Gradle wrapper (Windows)
```

## Architecture

The project follows a clean layered architecture with clear separation of concerns:

- **API Layer** (`api`): REST controllers exposing HTTP endpoints
- **Domain Layer** (`domain`): Core business logic and graph domain models
- **Engine Layer** (`engine`): Execution engine for processing graph operations
- **Persistence Layer** (`persistence`): Data access and repository implementations
- **Config Layer** (`config`): Spring configuration and application setup

## Prerequisites

- Java 21 or higher
- Docker (for Testcontainers integration tests)

No need to install Gradle separately - the project includes Gradle Wrapper.

## Getting Started

### Build the Project

```bash
cd backend
./gradlew build
```

On Windows:
```cmd
cd backend
gradlew.bat build
```

### Run the Application

```bash
cd backend
./gradlew bootRun
```

On Windows:
```cmd
cd backend
gradlew.bat bootRun
```

The application will start on `http://localhost:8080`

### Run Tests

```bash
cd backend
./gradlew test
```

## Technology Stack

- **Java 21**: Modern Java features and performance improvements
- **Spring Boot 3.2.0**: Framework for building production-ready applications
- **Spring Data JPA**: Data persistence and repository abstraction
- **Lombok**: Reduces boilerplate code
- **MapStruct**: Type-safe bean mapping
- **H2 Database**: In-memory database for development (default)
- **PostgreSQL**: Production database support
- **JUnit 5**: Testing framework
- **Testcontainers**: Docker-based integration testing

## API Endpoints

### Health Check
```
GET /api/health
```

Returns the application health status:
```json
{
  "status": "UP",
  "timestamp": "2024-01-01T12:00:00.000Z"
}
```

## Configuration

The application can be configured via `application.yml`. Key configuration options:

- **Server Port**: Default is 8080
- **Database**: H2 in-memory database (default), PostgreSQL support included
- **Logging**: Configurable log levels and patterns
- **H2 Console**: Available at `/h2-console` (development only)

### Database Configuration

#### Development (H2)
The default configuration uses H2 in-memory database. No additional setup required.

#### Production (PostgreSQL)
To use PostgreSQL, update `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/yourdb
    username: your_username
    password: your_password
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

## Testing

### Unit Tests
```bash
./gradlew test
```

### Integration Tests with Testcontainers

The project includes Testcontainers setup for PostgreSQL integration testing. Example usage:

```java
@SpringBootTest
@Import(PostgresTestcontainersConfig.class)
class MyIntegrationTest {
    // Tests will run against a real PostgreSQL instance in Docker
}
```

**Note**: Docker must be running for Testcontainers tests to work.

## Development

### IDE Setup

#### IntelliJ IDEA
1. Open the `backend` folder as a project
2. IntelliJ will automatically detect the Gradle project
3. Enable annotation processing for Lombok:
   - Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Check "Enable annotation processing"
4. Install Lombok plugin if prompted

#### VS Code
1. Install Java Extension Pack
2. Install Spring Boot Extension Pack
3. Open the `backend` folder
4. VS Code will automatically detect the Gradle project

### Code Style
- Follow standard Java naming conventions
- Use Lombok annotations to reduce boilerplate
- Keep layers separated (no circular dependencies)
- Write tests for business logic

## Building for Production

```bash
cd backend
./gradlew clean build
```

The executable JAR will be created at `backend/build/libs/backend-0.0.1-SNAPSHOT.jar`

Run the JAR:
```bash
java -jar backend/build/libs/backend-0.0.1-SNAPSHOT.jar
```

## Troubleshooting

### Port Already in Use
If port 8080 is already in use, you can change it in `application.yml`:
```yaml
server:
  port: 8081
```

Or via command line:
```bash
./gradlew bootRun --args='--server.port=8081'
```

### Gradle Build Fails
Try cleaning the build:
```bash
./gradlew clean build
```

### Tests Fail Due to Docker
Ensure Docker is running if you're running integration tests with Testcontainers.

## Next Steps

1. Define domain models in the `domain` package
2. Implement repositories in the `persistence` package
3. Create business logic in the `engine` package
4. Expose functionality through REST APIs in the `api` package
5. Add comprehensive tests for each layer

## License

TBD
