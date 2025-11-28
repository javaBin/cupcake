# syntax=docker/dockerfile:1.20

FROM --platform=$BUILDPLATFORM node:24-alpine AS build

WORKDIR /app
COPY . .

RUN npm ci
RUN npm run build

FROM node:24-alpine AS deploy

WORKDIR /app
COPY --from=build /app/.output/ /app
CMD ["node", "/app/server/index.mjs"]

