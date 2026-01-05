# ========================================
# Stage 1: Build Stage
# ========================================
FROM maven:3.9-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first for dependency caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies (this layer is cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN ./mvnw clean package -DskipTests -B

# ========================================
# Stage 2: Runtime Stage
# ========================================
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Create non-root user for security
RUN addgroup -S taskapp && adduser -S taskapp -G taskapp

# Create logs directory with proper permissions
RUN mkdir -p /logs && chown -R taskapp:taskapp /logs

# Copy JAR from builder stage
COPY --from=builder /app/target/TaskApp-*.jar app.jar

# Change ownership of application files
RUN chown -R taskapp:taskapp /app

# Switch to non-root user
USER taskapp

# Expose application port
EXPOSE 8080

# Health check using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM options for container environment
# -XX:+UseContainerSupport: Enable container awareness
# -XX:MaxRAMPercentage=75.0: Use up to 75% of container memory
# -Djava.security.egd=file:/dev/./urandom: Faster startup
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
