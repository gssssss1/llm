# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Core reactive model interfaces (`ReactiveModel`, `ModelInput`, `ModelOutput`)
- Chat model implementation with streaming support
- Embedding model interfaces
- Image model interfaces
- Tool/Function calling framework
- Model options with builder pattern and presets
- Message hierarchy supporting multiple roles
- Response format specifications (Text, JSON, JSON Schema)
- Token estimation and usage tracking
- Model capabilities and metadata system
- Cost tracking infrastructure with PricingInfo
- Type-safe sealed interfaces for controlled inheritance
- Comprehensive documentation (README, ARCHITECTURE, CONTRIBUTING)
- Example applications demonstrating framework usage
- Unit tests for core components

### Design Decisions
- Java 21+ as minimum version to leverage modern features
- Project Reactor for reactive programming
- Sealed interfaces for type safety
- Records for immutable data structures
- Builder pattern for complex object construction
- Fluent APIs for better developer experience

### Infrastructure
- Maven project structure
- Dependencies: Reactor, Jackson, Caffeine, Micrometer, Resilience4j
- Test framework setup with JUnit 5

## [1.0.0-SNAPSHOT] - 2024-01-XX

### Planned Features
- [ ] OpenAI adapter implementation
- [ ] Azure OpenAI adapter
- [ ] Anthropic Claude adapter
- [ ] Local model support (Ollama)
- [ ] RAG pipeline implementation
- [ ] Conversation memory management
- [ ] Prompt template engine
- [ ] Multi-tier caching (Caffeine + Redis)
- [ ] Semantic caching with vector similarity
- [ ] Interceptor chain implementation
- [ ] Retry strategies (Exponential backoff, fixed delay)
- [ ] Rate limiting (Token bucket, sliding window)
- [ ] HTTP client with connection pooling
- [ ] Observability (Metrics, Logging, Tracing)
- [ ] Tool executor implementation
- [ ] Tool chain orchestration
- [ ] Spring Boot starter
- [ ] Comprehensive integration tests
- [ ] Performance benchmarks
- [ ] More usage examples

### Documentation
- [x] README with quick start guide
- [x] Architecture design document
- [x] Contributing guidelines
- [x] Changelog
- [ ] API documentation
- [ ] User guide
- [ ] Best practices guide
- [ ] Migration guide from other frameworks
- [ ] Troubleshooting guide

### Known Limitations
- OpenAI adapter not yet implemented (stub code provided)
- No actual HTTP client implementation yet
- Cache implementations are interfaces only
- RAG components are planned but not implemented
- Memory management is designed but not coded

## Release Notes Template

### [X.Y.Z] - YYYY-MM-DD

#### Added
- New features

#### Changed
- Changes in existing functionality

#### Deprecated
- Soon-to-be removed features

#### Removed
- Now removed features

#### Fixed
- Bug fixes

#### Security
- Security fixes

---

## Version History

- **1.0.0-SNAPSHOT**: Initial framework design and core abstractions
  - Focus: Clean architecture, type safety, reactive patterns
  - Status: In Development
