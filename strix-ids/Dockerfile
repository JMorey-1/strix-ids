FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY settings.gradle .
COPY build.gradle .
COPY strix-ids strix-ids

RUN chmod +x gradlew
RUN ./gradlew :strix-ids:bootJar --no-daemon

FROM cgr.dev/chainguard/jre:latest

WORKDIR /app

COPY --from=build /app/strix-ids/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
