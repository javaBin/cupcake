# Frosting

Frontend

## TODO

Always lots to do - but - before we can release this:

* Require authentication - the backend needs to implement authentication/authorization.
* Some tests would be nice :)

## Build

Nuxt application using npm

    npm install

## Local running

    npm run dev

## Preview build

    npm build
    npm preview

## Deploy

Assuming we will build a docker container - add to [frontend action](../.github/workflows/frontend.yaml) when decided.

### Configuration

Set the environment variable `CUPCAKE_BACKEND` to the backend URL prefix (protocol, host and port, no trailing `/`). It
expects to find `/api`, `/login` and `/slackCallback` at this location.

If not set it will use `http://localhost:8080` for development.