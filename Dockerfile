# Use an official OpenJDK image as a base
FROM openjdk:17-jdk-slim AS build

WORKDIR /app

# Copy the Maven build files
COPY pom.xml ./
COPY mvnw ./
COPY .mvn/ .mvn/

# Ensure Maven wrapper is executable
RUN chmod +x mvnw

# Copy the project source
COPY src src

# Build the application
RUN ./mvnw package -DskipTests

# Use a smaller JDK image for the final image
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built JAR
COPY --from=build /app/target/SmartBlog-1.0.0.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the JAR file
CMD ["java", "-jar", "app.jar"]
