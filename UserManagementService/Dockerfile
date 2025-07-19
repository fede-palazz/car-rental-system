#################
## BUILD STAGE ##
#################
FROM openjdk:23-jdk-slim AS build

# Set working directory inside the container
WORKDIR /app

# Copy Gradle wrapper & build files first
COPY build.gradle.kts settings.gradle.kts /app/
COPY gradle /app/gradle
COPY gradlew /app/

# Convert line endings and give execution permissions
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# Download dependencies (caches this layer if build files haven't changed)
RUN ./gradlew build --no-daemon -x test || true

# Copy backend source code
COPY src /app/src

# Build the application (skip tests)
RUN ./gradlew bootJar --no-daemon -x test


###################
## RUNTIME STAGE ##
###################
FROM openjdk:23-jdk-slim

# Set working directory inside the container
WORKDIR /app

# Run as non-root user
RUN adduser --disabled-password --gecos "" appuser

# Switch to the non-root user
USER appuser

# Copy the built JAR file from the build stage to the container
COPY --from=build /app/build/libs/UserManagementService-0.0.1-SNAPSHOT.jar app.jar

# Set runtime environment variables
ENV CONTAINER=true
ENV SPRING_PROFILES_ACTIVE=prod

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]