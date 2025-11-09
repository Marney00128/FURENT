# Multi-stage build for Spring Boot (Java 17)

# 1) Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace
COPY pom.xml .
# Pre-fetch dependencies (better layer caching)
RUN mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -e -DskipTests package

# 2) Runtime stage (lightweight JRE)
FROM eclipse-temurin:17-jre
ENV JAVA_OPTS=""
WORKDIR /app
# Copy the jar built in previous step
# The artifact is mobiliarios-0.0.1-SNAPSHOT.jar according to pom.xml
COPY --from=builder /workspace/target/mobiliarios-0.0.1-SNAPSHOT.jar /app/app.jar
# Fly.io will set PORT env var. Spring uses server.port=${PORT:8080} configured in application.properties
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
