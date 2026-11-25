# syntax=docker/dockerfile:1
# Builds Taskly into one image: the Angular client is compiled first, then
# bundled into the Spring Boot jar, so the image always serves a fresh frontend.

# ---- Stage 1: build the Angular client ----
FROM node:24-slim AS frontend
WORKDIR /frontend
COPY ["Taskly Frontend/package.json", "Taskly Frontend/package-lock.json", "./"]
RUN npm ci --no-audit --no-fund
COPY ["Taskly Frontend/", "./"]
RUN npx ng build --configuration production

# ---- Stage 2: build the Spring Boot jar ----
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /backend
COPY ["Taskly Backend/pom.xml", "./"]
RUN mvn -B -q dependency:go-offline
COPY ["Taskly Backend/src", "./src"]
# Replace whatever bundle is checked in with the one built in stage 1.
RUN rm -rf src/main/resources/static && mkdir -p src/main/resources/static
COPY --from=frontend /frontend/dist/taskly-frontend/browser/ src/main/resources/static/
# Tests run in CI; skipping them here keeps image builds fast and database-free.
RUN mvn -B -q package -DskipTests

# ---- Stage 3: slim runtime image ----
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system taskly && useradd --system --gid taskly taskly
COPY --from=backend /backend/target/TMS-0.0.1-SNAPSHOT.jar app.jar
USER taskly
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
