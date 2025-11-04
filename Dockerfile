# Use Eclipse Temurin (official OpenJDK builds, replaces openjdk)
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Copy project files
COPY . .

# Make Maven wrapper executable
RUN chmod +x mvnw

# Build the Spring Boot JAR (skip tests for faster build)
RUN ./mvnw clean package -DskipTests


# ---- Runtime stage ----
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy built JAR from build stage
COPY --from=build /app/target/carry_guide_admin-0.0.1-SNAPSHOT.jar app.jar

# Expose port for Render
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
