# Build stage
FROM maven:3.9.12-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests package -DskipITs

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Install utilities needed by entrypoint
RUN apt-get update && apt-get install -y --no-install-recommends netcat curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/target/*.jar app.jar
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh
EXPOSE 9090
ENTRYPOINT ["/app/docker-entrypoint.sh"]
