# Demo deployment image. Build first: mvn -DskipTests package
FROM eclipse-temurin:25-jre

WORKDIR /app
COPY target/vibetracker-0.1.0.jar app.jar

# Render/Railway/Fly inject PORT; Spring maps SERVER_PORT itself, so no shell tricks.
ENV SERVER_PORT=8080
# The prod profile uses an in-memory database so a restart re-seeds the demo data
# instead of depending on a writable disk.
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
