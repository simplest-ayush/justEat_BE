# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom first (layer-cached; only re-downloaded when pom changes)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Ensure mvnw is executable (Windows → Linux line endings are safe in Alpine)
RUN chmod +x mvnw

# Download all dependencies offline so subsequent builds are faster
RUN ./mvnw dependency:go-offline -B

# Copy source and build the fat JAR (tests are already verified in CI)
COPY src ./src
RUN ./mvnw package -DskipTests -B

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy only the fat JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8090

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]