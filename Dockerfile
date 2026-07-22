# Stage 1: Build
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy wrapper and run empty task to cache Gradle distribution early
COPY gradle gradle
COPY gradlew .
RUN ./gradlew --no-daemon

# Copy config files
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Copy module build files
COPY insurahub-api/build.gradle.kts insurahub-api/
COPY insurahub-domain/build.gradle.kts insurahub-domain/
COPY insurahub-implementation/build.gradle.kts insurahub-implementation/

# Download dependencies
RUN ./gradlew :insurahub-implementation:dependencies \
    --no-daemon \
    --stacktrace \
    --console=plain

# Copy OpenAPI and generate sources
COPY insurahub-api/openapi insurahub-api/openapi/
RUN ./gradlew :insurahub-api:openApiGenerate \
    --no-daemon \
    --stacktrace \
    --console=plain

# Copy source code and build
COPY insurahub-domain/src insurahub-domain/src/
COPY insurahub-implementation/src insurahub-implementation/src/
RUN ./gradlew :insurahub-implementation:bootJar \
    -x test \
    --no-daemon \
    --stacktrace \
    --console=plain

# Stage 2: Run
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/insurahub-implementation/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]