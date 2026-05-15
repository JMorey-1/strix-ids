FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY settings.gradle .
COPY build.gradle .
COPY traffic-generator traffic-generator

RUN chmod +x gradlew
RUN ./gradlew :traffic-generator:installDist --no-daemon

FROM cgr.dev/chainguard/jre:latest

WORKDIR /app

COPY --from=build /app/traffic-generator/build/install/traffic-generator/ .

ENTRYPOINT ["java", "-cp", "lib/*", "org.example.StrixGenerator"]
