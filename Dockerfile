# --- build stage ------------------------------------------------------------
# Maven runs inside the image, so a fresh clone can be deployed without a local
# build and without committing a 55 MB JAR.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

# --- run stage --------------------------------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/target/vibetracker-0.1.0.jar app.jar

# Hosted demo: in-memory database, re-seeded on every boot (see application-prod.yml).
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080
# Render/Railway/Fly inject PORT; fall back to 8080 for a plain `docker run`.
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]
