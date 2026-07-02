# Stage 1: Build
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY gradle gradle
COPY gradlew .
RUN ./gradlew
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY insurahub-api/build.gradle.kts insurahub-api/
COPY insurahub-api/openapi insurahub-api/openapi/
RUN ./gradlew :insurahub-api:openApiGenerate
COPY insurahub-implementation/build.gradle.kts insurahub-implementation/
COPY insurahub-implementation/src insurahub-implementation/src/
RUN ./gradlew :insurahub-implementation:bootJar -x test

# Stage 2: Run
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/insurahub-implementation/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
