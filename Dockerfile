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
COPY insurahub-implementation/build.gradle.kts insurahub-implementation/

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy OpenAPI and generate sources
COPY insurahub-api/openapi insurahub-api/openapi/
RUN ./gradlew :insurahub-api:openApiGenerate --no-daemon

# Copy source code and build
COPY insurahub-implementation/src insurahub-implementation/src/
RUN ./gradlew :insurahub-implementation:bootJar -x test --no-daemon

# Stage 2: Run
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/insurahub-implementation/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
