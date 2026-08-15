# Stage 1: Build Spring Boot application
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Copy Maven project files
COPY pom.xml .

# Copy source code
COPY src ./src

# Build the Spring Boot JAR
RUN ./mvnw clean package -DskipTests 2>/dev/null || \
    mvn clean package -DskipTests


# Stage 2: Run application
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]