# Backend Module

Spring Boot backend application with modular architecture.

## Quick Start

### Build
```bash
./gradlew build
```

### Run
```bash
./gradlew bootRun
```

The application starts at `http://localhost:8080`

### Test
```bash
./gradlew test
```

## Structure

- `api/` - REST controllers and API layer
- `domain/` - Core business domain and graph models
- `engine/` - Execution engine for graph processing
- `persistence/` - JPA repositories and data access
- `config/` - Spring configuration

## Health Check

```bash
curl http://localhost:8080/api/health
```

Response:
```json
{
  "status": "UP",
  "timestamp": "2024-01-01T12:00:00.000Z"
}
```

See main [README.md](../README.md) for full documentation.
