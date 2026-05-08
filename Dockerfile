# syntax=docker/dockerfile:1.23

FROM eclipse-temurin:22-jdk AS build

WORKDIR /build
COPY . .

RUN ./gradlew clean build

FROM eclipse-temurin:22-jre AS deploy

WORKDIR /opt/app
COPY --from=build /build/build/libs/cupcake.jar /opt/app

CMD ["java", "-jar", "/opt/app/cupcake.jar"]
