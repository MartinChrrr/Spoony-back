# syntax=docker/dockerfile:1.7

# The Spring Boot jar is architecture-neutral: compile on the native CI
# platform, then package it in the requested ARM64 runtime image. This avoids
# running Maven under QEMU during a Graviton build.
FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jdk-alpine@sha256:0bfc69a4758a86710e5c474032d28400a8bd00874766f9e8b1642ac2fd293159 AS build
WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw
COPY src ./src
# BuildKit persists Maven artifacts between builds. Running dependency:go-offline
# here downloaded the complete Maven plugin graph (including unused optional
# BOMs) and made cold CI builds several minutes slower than the actual package.
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw clean package -Dmaven.test.skip=true -B

FROM --platform=$TARGETPLATFORM eclipse-temurin:21-jre-alpine@sha256:51ab5e3302e7141ce665ca3ea85e8b5cd648eafbc3c0c90dd79d6537684e4555
RUN addgroup -S spoony && adduser -S spoony -G spoony
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN chown spoony:spoony app.jar
USER spoony
# Default to the prod profile so JSON logging + secret resolution are active even
# if the orchestrator forgets to pass it. Without an active profile the common
# application.yml has no datasource/jwt block, JWT_SECRET stays an unresolved
# placeholder and the Spring context fails at boot (crash-loop). Defense in depth:
# the production Compose definition also sets SPRING_PROFILES_ACTIVE explicitly.
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
# start-period 60s: a small ARM instance cold start can exceed 30s.
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
  CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1
# ExitOnOutOfMemoryError: let the orchestrator restart a dead JVM rather than keep
# a zombie that still answers the health check.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "app.jar"]
