# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:21-jdk-alpine@sha256:6ea5548706b60ac0a602eaf48af74792cbab012d90e811ca8db6184b16b5c3d6 AS build
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
    ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine@sha256:974b08960c5d96694c780e65b2d5705268ab1e1ca1a0dd0caf4ba6c3fe34d699
RUN addgroup -S spoony && adduser -S spoony -G spoony
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN chown spoony:spoony app.jar
USER spoony
# Default to the prod profile so JSON logging + secret resolution are active even
# if the orchestrator forgets to pass it. Without an active profile the common
# application.yml has no datasource/jwt block, JWT_SECRET stays an unresolved
# placeholder and the Spring context fails at boot (crash-loop). Defense in depth:
# the ECS task definition also sets SPRING_PROFILES_ACTIVE=prod explicitly.
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
# start-period 60s: Fargate cold start + Flyway migrations can exceed 30s.
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
  CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1
# ExitOnOutOfMemoryError: let the orchestrator restart a dead JVM rather than keep
# a zombie that still answers the health check.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "app.jar"]
