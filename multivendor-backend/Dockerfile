# Stage 1: Build the Spring Boot application with Maven
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml first so dependency layer is cached separately from source changes
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Copy source and build the executable JAR (tests skipped for faster image builds)
COPY src src
RUN mvn -q clean package -DskipTests

# Stage 2: Lightweight runtime image
FROM eclipse-temurin:17-jre-focal
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
