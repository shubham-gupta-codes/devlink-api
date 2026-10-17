# Multi-stage Dockerfile for DevLink Backend (Java 21 + Spring Boot 4)
# Stage 1: Build the JAR
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Gradle wrapper and configuration files
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
RUN chmod +x ./gradlew

# Pre-fetch dependencies
RUN ./gradlew dependencies --no-daemon || true

# Copy source code and build the executable JAR
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Lightweight JRE runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create directory for file uploads
RUN mkdir -p uploads

# Copy compiled JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Dynamic port for Railway
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
