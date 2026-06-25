FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
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
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1
# ExitOnOutOfMemoryError: let the orchestrator restart a dead JVM rather than keep
# a zombie that still answers the health check.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "app.jar"]
