# syntax=docker/dockerfile:1.21

FROM --platform=$BUILDPLATFORM eclipse-temurin:22 AS build

WORKDIR /build
COPY . .

RUN ./gradlew clean build

FROM eclipse-temurin:22-jre AS deploy

WORKDIR /opt/app
COPY --from=build /build/build/libs/cupcake.jar /opt/app

CMD ["java", "-jar", "/opt/app/cupcake.jar"]
