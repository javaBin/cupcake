# Cupcake

A Kotlin/Ktor backend that aggregates JavaZone conference and session data from [Sleeping Pill](https://sleepingpill.javazone.no) and enriches speaker profiles with Norwegian postal code data from the Bring API. It serves as the API backend for [frosting](https://github.com/javaBin/frosting), enabling javaBin regions to discover potential speakers for local events.

## Tech stack

- **Language**: Kotlin (JVM 22)
- **Framework**: Ktor
- **Build**: Gradle (Kotlin DSL)
- **Caching**: Caffeine (conference/session data) + Cache4k (postal codes)
- **Auth**: OIDC/JWT via configurable discovery endpoint
- **Metrics**: Micrometer + Prometheus

## API endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/conferences` | Lists conferences (filtered by year, sorted by name descending) |
| `GET` | `/api/conferences/{id}/sessions` | Sessions for a conference, with speaker postal code/city/county data |
| `GET` | `/api/me` | Authenticated user info from OIDC token |

Additional endpoints (`/login`, `/refresh`) are handled by the OIDC layer.

## Build

```bash
./gradlew clean build
```

Code quality checks (Detekt, Kotlinter, JaCoCo coverage) run as part of `check`:

```bash
./gradlew check
```

The output artifact is `build/libs/cupcake.jar` (fat JAR with all dependencies bundled).

## Local running

Set the following environment variables (a `local.env` file in the root is gitignored and works well with the IntelliJ IDEA EnvFile plugin):

| Variable | Description |
|---|---|
| `SP_BASE_URL` | Sleeping Pill base URL (e.g. `https://sleepingpill.javazone.no`) |
| `SP_USER` | Sleeping Pill username |
| `SP_PASSWORD` | Sleeping Pill password |
| `BRING_API_KEY` | Bring API key |
| `BRING_API_USER` | Bring API user (email) |
| `OIDC_WELL_KNOWN_URL` | OIDC discovery endpoint (e.g. `https://auth.example.com/realms/myrealm/.well-known/openid-configuration`) |
| `OIDC_EXPECTED_AZP` | Expected OIDC client ID (defaults to `cupcake-client`) |
| `JWT_ENABLED` | `true` to enforce JWT authentication, `false` to disable |

Then run:

```bash
./gradlew run
```

The server starts on port 8080. With `JWT_ENABLED=false`, no auth is required and `localhost` is fine.

## Docker

Multi-platform images (`linux/amd64`, `linux/arm64`) are published to `ghcr.io/javabin/cupcake`.

To build locally:

```bash
docker build -t cupcake .
```

The image uses a multi-stage build (Eclipse Temurin JDK 22 build stage, JRE runtime stage) and runs on port 8080.

## CI/CD

| Trigger | Workflow | What it does |
|---|---|---|
| Push to `main` | `build.yaml` | Runs `check`, builds and pushes multi-platform Docker image, tags as `staging` |
| Pull request | `pr.yaml` | Runs `check` (tests, linting, analysis) |
| Tag `v*` | `release.yaml` | Promotes `staging` image to `release` and version tag |

## Deploy

Example hostnames:

```
https://cupcake-backend.java.no  →  backend (this service)
https://cupcake.java.no          →  frontend (frosting)
```

If deploying with Docker Compose or a shared Docker network, or inside kubernetes with access, use the service name as the `CUPCAKE_BACKEND` value.

### Environment variables for deployment

#### JWT / OIDC (backend)

| Variable | Value |
|---|---|
| `JWT_ENABLED` | `true` |
| `OIDC_WELL_KNOWN_URL` | OIDC discovery endpoint |
| `OIDC_EXPECTED_AZP` | Expected client ID (defaults to `cupcake-client`) |

Users must have the `pkom` role assigned in the OIDC provider under the client specified by `OIDC_EXPECTED_AZP`.

#### OIDC (frontend — must match backend)

| Variable | Description |
|---|---|
| `NUXT_PUBLIC_OIDC_AUTHORITY` | OIDC authority URL (e.g. `https://auth.example.com/realms/myrealm`) |
| `NUXT_PUBLIC_OIDC_CLIENT_ID` | OIDC client ID (defaults to `cupcake-client`) |

See the [frosting README](https://github.com/javaBin/frosting/README.md) for full frontend configuration.

#### Sleeping Pill

| Variable | Description |
|---|---|
| `SP_BASE_URL` | Sleeping Pill base URL |
| `SP_USER` | Username |
| `SP_PASSWORD` | Password |

#### Bring

| Variable | Description |
|---|---|
| `BRING_API_USER` | Bring API user (email) |
| `BRING_API_KEY` | Bring API key |
