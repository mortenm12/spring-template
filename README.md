# spring-template

Production-ready Spring Boot microservice template.

## Stack

| Layer | Technology |
|---|---|
| Runtime | JDK 25 (Virtual Threads / Project Loom) |
| Framework | Spring Boot 3.5 |
| Build | Gradle 8 (Kotlin DSL) |
| Persistence | PostgreSQL 17 + Flyway + Spring Data JPA |
| Security | OAuth2 Resource Server (JWT) |
| Observability | Micrometer + Prometheus + OpenTelemetry (OTLP) |
| API Docs | SpringDoc OpenAPI 3.1 + Swagger UI |
| Resilience | Resilience4j (Circuit Breaker, Retry, Rate Limiter) |
| Logging | Spring Boot ECS structured JSON logging |
| Testing | JUnit 5 + Mockito + Testcontainers (real PostgreSQL) |

## Quick Start

```bash
# 1. Start PostgreSQL locally
docker compose up db -d

# 2. Run with local profile
./gradlew bootRun --args='--spring.profiles.active=local'

# 3. Browse API documentation
open http://localhost:8080/swagger-ui.html

# 4. Health check
curl http://localhost:8080/actuator/health
```

## Running Tests

```bash
./gradlew test
```

Tests use Testcontainers — Docker must be running on your machine.

## Local Observability Stack

```bash
# Start app + PostgreSQL + Prometheus + Grafana
docker compose up

# Prometheus: http://localhost:9090
# Grafana:    http://localhost:3000  (admin / admin)
# Metrics:    http://localhost:8080/actuator/prometheus
```

## Configuration Reference

| Environment Variable | Default | Description |
|---|---|---|
| `JWT_JWK_SET_URI` | `http://localhost:9000/oauth2/jwks` | JWKS endpoint for JWT validation |
| `DB_URL` | — | JDBC URL (required in prod) |
| `DB_USERNAME` | — | Database username |
| `DB_PASSWORD` | — | Database password |

## Profiles

| Profile | Purpose |
|---|---|
| *(default)* | Base configuration — do not run directly |
| `local` | Local development — DB on localhost:5432 |
| `test` | Testcontainers integration tests |
| `prod` | Production — all config via environment variables |

## Project Structure

```
src/main/java/com/example/template/
├── api/                    # REST controllers and DTOs (Java Records)
│   ├── ItemController.java
│   └── dto/
├── config/                 # Spring configuration beans
│   ├── OpenApiConfig.java
│   └── SecurityConfig.java
├── domain/                 # JPA entities
│   └── Item.java
├── exception/              # Exception types and global handler (RFC 7807 ProblemDetail)
│   ├── ResourceNotFoundException.java
│   └── GlobalExceptionHandler.java
├── repository/             # Spring Data JPA repositories
│   └── ItemRepository.java
└── service/                # Business logic
    └── ItemService.java
```

## Adapting This Template

1. Rename package `com.example.template` to match your service (find-and-replace in IDE).
2. Replace `Item` / `items` with your domain entity.
3. Update `V1__init.sql` with your actual schema.
4. Update `OpenApiConfig` with your service title and contact info.
5. Set `JWT_JWK_SET_URI` to your authorization server's JWKS endpoint.
6. Adjust security scopes in `SecurityConfig` to match your OAuth2 scopes.

## Key Design Decisions

**Virtual Threads over WebFlux** — JDK 25 virtual threads give reactive-level throughput on blocking code (JDBC, JPA) without the complexity of reactive programming. Activated via `spring.threads.virtual.enabled=true`.

**Flyway over Hibernate DDL** — `ddl-auto: validate` means Hibernate never touches the schema. All changes go through versioned SQL migration files that are code-reviewed and auditable.

**ProblemDetail (RFC 7807)** — Error responses use Spring 6's native `ProblemDetail` type, serialized as `application/problem+json`. Stable `type` URIs enable deterministic client-side error handling across all services.

**Testcontainers over H2** — Integration tests run against a real PostgreSQL instance. H2 compatibility mode has subtle differences (UUID types, `gen_random_uuid()`, JSONB) that can mask production bugs.

**ECS Structured Logging** — `logging.structured.format.console: ecs` (Spring Boot 3.4+) outputs Elastic Common Schema JSON to stdout with zero configuration. Compatible with Loki, OpenSearch, Datadog, and Elastic out of the box.
