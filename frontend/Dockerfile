# syntax=docker/dockerfile:1.21

FROM --platform=$BUILDPLATFORM node:24-bookworm-slim AS build

ENV PNPM_HOME="/pnpm"
ENV PATH="$PNPM_HOME:$PATH"
RUN corepack enable

WORKDIR /app

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./

RUN pnpm install --frozen-lockfile

COPY . .

RUN pnpm run build

FROM node:24-bookworm-slim AS deploy

ENV NODE_ENV=production

WORKDIR /app

COPY --from=build /app/.output ./

CMD ["node", "/app/server/index.mjs"]

