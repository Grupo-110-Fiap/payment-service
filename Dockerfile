# Stage 1: Build
FROM gradle:8.4-jdk21 AS build
WORKDIR /app

# Copy only the files needed for dependency resolution to leverage Docker cache
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle gradle

# Prefetch dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source code and build
COPY src src
RUN ./gradlew build -x test --no-daemon

# Stage 2: Run
FROM eclipse-temurin:21-jre AS runner

# Create a non-root user
RUN groupadd -r appgroup && useradd -r -g appgroup appuser
USER appuser

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
