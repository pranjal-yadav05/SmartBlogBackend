# ---- Build stage ----
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copy Maven build files
COPY pom.xml ./
COPY mvnw ./
COPY .mvn/ .mvn/

RUN chmod +x mvnw

# Copy source
COPY src src

# Build the application
RUN ./mvnw package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built JAR
COPY --from=build /app/target/SmartBlog-1.0.0.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
