# Step 1: Build the JAR using a full JDK
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only pom.xml first to leverage Docker caching for dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the project and build
COPY src ./src
RUN mvn clean package -DskipTests

# Step 2: Use a lightweight Temurin JRE for runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
