# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Is

**Cupcake** is a Kotlin/Ktor REST API backend for javaBin regional organizers to discover potential speakers for local events. It aggregates JavaZone conference/session data from Sleeping Pill and enriches speaker profiles with Norwegian postal code information from the Bring API. The frontend (Frosting) lives in a separate repository.

## Commands

All commands run from the repository root (where `gradlew` lives).

```bash
./gradlew run                    # Run locally (port 8080)
./gradlew clean build            # Build fat JAR → build/libs/cupcake.jar
./gradlew check                  # Lint (detekt + kotlinter) + tests + coverage
./gradlew test                   # Run all tests
./gradlew test --tests "ClassName"          # Run a single test class
./gradlew test --tests "*.ClassName.test*"  # Run tests matching a pattern
./gradlew lintKotlin             # Kotlinter check only
./gradlew detekt                 # Detekt check only
```

## Environment Variables

Create a `local.env` file in the root (gitignored) for local development. Required vars:

| Variable | Description |
|---|---|
| `SP_BASE_URL` | Sleeping Pill base URL |
| `SP_USER` / `SP_PASSWORD` | Sleeping Pill credentials |
| `BRING_API_USER` / `BRING_API_KEY` | Bring API credentials (email + key) |
| `OIDC_WELL_KNOWN_URL` | OIDC discovery endpoint |
| `JWT_ENABLED` | Set to `false` to disable auth locally |

Optional: `SP_CACHE_TTL_SECONDS` (default 3600), `PORT` (default 8080).

## Architecture

### Package Layout (`src/main/kotlin/no/java/cupcake/`)

- **`plugins/`** — Ktor plugin configuration: `Routing.kt`, `Security.kt`, `Monitoring.kt`, `HTTP.kt`, `Serialization.kt`
- **`clients/`** — HTTP clients for Sleeping Pill and Bring API
- **`sleepingpill/`** — Domain models (`Conference`, `Session`) and `SleepingPillService` with Caffeine async cache
- **`bring/`** — `BringService` with Cache4k postal code cache (24h TTL, refreshed hourly)
- **`api/`** — `ApiError` sealed class and `Respond.kt` extension functions
- **`config/`** — Configuration data classes (loaded from `application.conf` via HOCON)

### API Endpoints

All require JWT authentication with `helter` Cognito group membership (unless `JWT_ENABLED=false`):

- `GET /api/conferences` — Conferences filtered by year, sorted by name descending
- `GET /api/conferences/{id}/sessions` — Sessions enriched with speaker postal code/city/county
- `GET /api/me` — Authenticated user info from Cognito UserInfo endpoint + token claims
- `GET /metrics-micrometer` — Prometheus metrics (unauthenticated)

### Key Patterns

**Error handling**: Arrow's `Either<ApiError, T>` throughout. `ApiError` is a sealed class; `RoutingContext.respond(error: ApiError)` in `Respond.kt` maps errors to HTTP responses.

**Caching**: Sleeping Pill data uses Caffeine async cache (configurable TTL). Postal codes use Cache4k (24h TTL). Setting TTL ≤ 0 disables caching.

**Security**: JWT validated via Auth0 JWK provider against Cognito's JWKS. Requires `helter` group in `cognito:groups` claim. `/api/me` calls the Cognito UserInfo endpoint with the Bearer token to retrieve email and name; `hasPkomRole` is derived from `cognito:groups` containing `pkom`.

**Serialization**: kotlinx.serialization. Custom serializer for `HttpStatusCode`.

### Testing

Tests use Kotest (FunSpec) with Ktor's test host. Key utilities in `TestExtensions.kt`:
- `buildClient()` / `buildMockEngine()` — mocked HTTP client with fixture responses
- `serializedTestApplication()` — test app with serialization and no-op auth
- `loadFixture()` — loads JSON from `src/test/resources/`

Fixtures live in `src/test/resources/` (e.g., `conferences.json`, `sessions.json`, `postal_codes.json`).
