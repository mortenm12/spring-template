# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> **Dette er et service template.** Når templaten klones til et nyt projekt, skal denne CLAUDE.md opdateres, så den afspejler det nye projekts domæne, pakkenavn og konventioner — fjern eller erstat al template-specifik vejledning.

## Commands

```bash
# Run the app locally (requires Docker for PostgreSQL + RabbitMQ)
docker compose up db rabbitmq -d
./gradlew bootRun --args='--spring.profiles.active=local'

# Run all tests (requires Docker — Testcontainers spins up real PostgreSQL + RabbitMQ)
./gradlew test

# Run a single test class
./gradlew test --tests "com.example.template.api.ItemControllerTest"

# Run a single test method
./gradlew test --tests "com.example.template.api.ItemControllerTest.createItem_returns201WithLocation"

# Lint (Checkstyle — fails on violations)
./gradlew checkstyleMain checkstyleTest

# Build (compile only, no tests)
./gradlew classes

# Build the fat JAR
./gradlew bootJar   # output: build/libs/app.jar

# Full observability stack (app + DB + RabbitMQ + Prometheus + Grafana)
docker compose up
```

## Architecture

This is a Spring Boot 3.5 microservice template using JDK 25 virtual threads (`spring.threads.virtual.enabled=true`). The main package is `com.example.template`.

**Layer flow:** `ItemController` → `ItemService` → `ItemRepository` (JPA) + `ItemEventPublisher` (RabbitMQ)

### Key design decisions

- **No Hibernate DDL** — `ddl-auto: validate` only. All schema changes go through versioned Flyway migrations in `src/main/resources/db/migration/`. Never use `create`, `update`, or `create-drop`.
- **ProblemDetail (RFC 7807)** — All error responses are `application/problem+json` produced by `GlobalExceptionHandler`. Problem type URIs are anchored to `https://api.example.com/problems/`. When adding new exception types, add a handler here and assign a stable `type` URI.
- **ECS structured logging** — `logging.structured.format.console: ecs` outputs JSON to stdout. Do not configure file appenders or custom log formats.
- **OAuth2 scopes** — GET `/api/**` requires `SCOPE_read`; all other `/api/**` routes require `SCOPE_write`. Public paths (actuator health, Swagger UI, API docs) are in `SecurityConfig.PUBLIC_PATHS`.

### Messaging

RabbitMQ is configured in `RabbitMqConfig` with a single topic exchange (`items.exchange`) and queue (`items.queue`) bound via `item.#`. Routing keys follow the pattern `item.<action>` (e.g. `item.created`). `ItemEventPublisher` is the only outbound gateway — always go through it, not `RabbitTemplate` directly. `ItemEventListener` consumes from `items.queue`.

### Testing conventions

- **Controller tests** use `@WebMvcTest` + `@Import(SecurityConfig.class)` + `SecurityMockMvcRequestPostProcessors.jwt()` to exercise the real security filter chain with a mock JWT.
- **Integration tests** use Testcontainers via the `tc:` JDBC URL (`jdbc:tc:postgresql:17-alpine:///templatedb`) — the `test` profile activates this automatically. No `@SpringBootTest` context is needed for controller-layer tests.
- `@MockitoBean` (Spring Boot 3.4+) replaces the older `@MockBean`.

### Checkstyle rules

- No wildcard imports (`AvoidStarImport`)
- No tabs — spaces only (`FileTabCharacter`)
- Files must end with a newline (`NewlineAtEndOfFile` with LF)
- All blocks must have braces (`NeedBraces`)
- `equals` and `hashCode` must be overridden together (`EqualsHashCode`)

## Profiles

| Profile | Purpose |
|---|---|
| `local` | Dev — PostgreSQL on `localhost:5432`, credentials `template/template` |
| `test` | Testcontainers — datasource URL drives container lifecycle |
| `prod` | All config via env vars: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_JWK_SET_URI`, `RABBITMQ_HOST` |

## CI / GitHub Actions

Alle tre workflows skal være grønne før en PR merges:

- **Build & Test** — kompilerer, kører tests med Testcontainers og bygger Docker-image
- **Lint (Checkstyle)** — validerer kodestil; fejl blokerer merge
- CI kører på push til `main`/`develop` og på alle PRs mod `main`

Kør altid `./gradlew test` og `./gradlew checkstyleMain checkstyleTest` lokalt inden push.

## SOLID-principper

Følg SOLID konsekvent i al ny kode:

- **Single Responsibility** — hver klasse har ét ansvarsområde. Controllers håndterer HTTP, services håndterer forretningslogik, repositories håndterer persistens. Bland ikke lagene.
- **Open/Closed** — udvid adfærd via nye klasser eller strategier frem for at modificere eksisterende. Brug f.eks. nye `@ExceptionHandler`-metoder i `GlobalExceptionHandler` frem for at rode i eksisterende handlers.
- **Liskov Substitution** — subtyper skal kunne træde i stedet for deres basistype uden at ændre programmets korrekthed.
- **Interface Segregation** — foretrækk smalle, fokuserede interfaces frem for brede. Spring Data-repositories er et godt eksempel; definér kun de query-metoder der faktisk bruges.
- **Dependency Inversion** — afhæng af abstraktioner (interfaces), ikke konkrete implementeringer. Injicer afhængigheder via konstruktør (som eksisterende kode gør) — undgå field injection og `@Autowired`.

## Adapting the template

1. Rename package `com.example.template` → your package (find-and-replace).
2. Replace `Item`/`items` with your domain entity throughout all layers.
3. Replace `V1__init.sql` with your schema.
4. Update `OpenApiConfig` with your service title/contact.
5. Set `JWT_JWK_SET_URI` and adjust OAuth2 scopes in `SecurityConfig`.
6. Update `PROBLEM_BASE_URI` in `GlobalExceptionHandler` to your API domain.
