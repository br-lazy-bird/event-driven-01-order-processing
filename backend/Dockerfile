# Multi-stage build for Spring Boot application

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy parent POM first
COPY pom.xml .

# Install parent POM to local repository
RUN mvn clean install -N

# Copy common module
COPY common/pom.xml ./common/
COPY common/src ./common/src

# Build common module
RUN cd common && mvn clean install -DskipTests

# Copy backend module
COPY backend/pom.xml ./backend/
COPY backend/src ./backend/src

# Build backend
RUN cd backend && mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/backend/target/*.jar app.jar

# Expose port
EXPOSE 8000

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
