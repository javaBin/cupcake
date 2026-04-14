# Frosting

Frontend

## TODO

Always lots to do - but - before we can release this:

- Some tests would be nice :)

## Build

Nuxt application using pnpm

    pnpm install

## Local running

    pnpm dev

## Preview build

    pnpm build
    pnpm preview

## Deploy

Assuming we will build a docker container - add to [frontend action](../.github/workflows/frontend.yaml) when decided.

### Configuration

| Environment Variable       | Description                                              | Default                                        |
|----------------------------|----------------------------------------------------------|------------------------------------------------|
| `CUPCAKE_BACKEND`          | Backend base URL for the server-side proxy               | `http://127.0.0.1:8080` (dev) / `https://cupcake-backend.java.no` (prod) |
| `CUPCAKE_FRONTEND`         | Hostname the dev server allows (Vite `allowedHosts`)     | `localhost`                                    |
| `NUXT_PUBLIC_OIDC_AUTHORITY` | OIDC authority URL (e.g. Keycloak realm URL)           | `https://auth.home.chrissearle.org/realms/HA12` |
| `NUXT_PUBLIC_OIDC_CLIENT_ID` | OIDC client ID                                         | `cupcake-client`                               |
