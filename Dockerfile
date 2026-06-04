# --- Build stage: compile and package the Spring Boot jar ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src/ src/
# Skip tests here (they run in CI, incl. Testcontainers which needs a Docker daemon).
RUN ./mvnw -B -DskipTests clean package

# --- Runtime stage: minimal, non-root distroless image ---
FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app
COPY --from=build /workspace/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
