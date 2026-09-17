# syntax=docker/dockerfile:1.27

FROM eclipse-temurin:25-jdk AS build

WORKDIR /app
COPY . .

RUN ./gradlew clean installDist \
    && mkdir -p /app/appjar \
    && mv /app/build/install/cupcake/lib/cupcake*.jar /app/appjar/

FROM eclipse-temurin:25-jre AS deploy

COPY --from=build /app/build/install/cupcake/bin /opt/app/bin
COPY --from=build /app/build/install/cupcake/lib /opt/app/lib

COPY --from=build /app/appjar/ /opt/app/lib/

CMD ["/opt/app/bin/cupcake"]
