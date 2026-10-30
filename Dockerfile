# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /src

# Cache dependency downloads
COPY pom.xml ./
RUN mvn -q -o dependency:go-offline 2>/dev/null || mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /src/target/time-microservice-*.jar /app/time-microservice.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/time-microservice.jar"]
