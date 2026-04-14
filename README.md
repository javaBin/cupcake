# Cupcake

A minimal server providing a read-only version of [cake](https://github.com/javaBin/cake-redux) for javaBin regions to
be able to find possible speakers for local talks.

For frontend - see [frosting](./frontend)

## Build

Gradle application using ktor.

## Local running

You will require env variables:

    SP_BASE_URL=https://sleepingpill.javazone.no
    SP_USER=<username>
    SP_PASSWORD=<password>
    BRING_API_KEY=<key>
    BRING_API_USER=<e-mail>
    OIDC_WELL_KNOWN_URL=<OIDC discovery endpoint URL, e.g. https://auth.example.com/realms/myrealm/.well-known/openid-configuration>
    OIDC_EXPECTED_AZP=<expected client ID - defaults to "cupcake-client">
    JWT_ENABLED=true/false

Note - the frontend OIDC authority and client ID are currently hardcoded in
`frontend/app/composables/useAuth.ts`. If you need to use a different OIDC provider locally,
update `AUTHORITY` and `CLIENT_ID` in that file.

If you are not running with auth (`JWT_ENABLED=false`) then localhost is fine.

## Local running with docker compose

You will need to provide the same environment variables as above in a file called `local.env` in the root directory.

Note - this file is also useful when running from Idea with the envfile plugin.

This file MUST NOT be committed to git (it is in .gitignore).

## Deploy

Assuming we will build a docker container - add to [backend action](./.github/workflows/backend.yaml) when decided.

Currently it is setup for the frontend to proxy the backend - anything on `/api/*`

For example - let's say we setup:

    https://cupcake_backend.javazone.no -> backend
    https://cupcake.javazone.no -> frontend

We would then need to set the host in the frontend for non development builds to `https://cupcake_backend.javazone.no` in
the [proxy](./frontend/server/middleware/proxy.ts) file - but this is set using the CUPCAKE_BACKEND env var.

All app configuration for the backend is done via the environment.

If deploying with docker - you can place both on the same docker network and use the service name for the env var - see
[docker-compose.yml](./docker-compose.yml) for an example.

### JWT

    JWT_ENABLED - true

### Sleepingpill

We use the same user and password for dev and deploy here but it must be set in the environment.

    SP_USER
    SP_PASSWORD

### Bring

We use the same user and password for dev and deploy here but it must be set in the environment.

    BRING_API_USER
    BRING_API_KEY

### OIDC

This provides authentication and access checking. Users must have the `pkom` role assigned
in the OIDC provider under the client specified by `OIDC_EXPECTED_AZP`.

    OIDC_WELL_KNOWN_URL - the OIDC discovery endpoint (e.g. https://auth.example.com/realms/myrealm/.well-known/openid-configuration)
    OIDC_EXPECTED_AZP - the expected client ID (defaults to "cupcake-client")

The backend fetches the JWKS from the discovery document and validates incoming tokens against it.

Note - the frontend has the OIDC authority and client ID hardcoded in
`frontend/app/composables/useAuth.ts` (`AUTHORITY` and `CLIENT_ID` constants). These must be
kept in sync with the backend `OIDC_WELL_KNOWN_URL` and `OIDC_EXPECTED_AZP` settings.

