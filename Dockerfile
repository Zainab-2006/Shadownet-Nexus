# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY springboot/pom.xml springboot/
COPY springboot/.mvn springboot/.mvn
COPY springboot/mvnw springboot/

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN cd springboot && ./mvnw dependency:go-offline -B

# Copy source code
COPY springboot/src springboot/src

# Build the application
RUN cd springboot && ./mvnw clean package -DskipTests

# Expose port
EXPOSE 3001

# Run the application
CMD ["java", "-jar", "springboot/target/shadownet-nexus-1.0.0.jar"]