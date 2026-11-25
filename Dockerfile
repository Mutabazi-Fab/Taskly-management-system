# syntax=docker/dockerfile:1
# Builds Taskly into one image. Maven compiles the Angular client and bundles it
# into the Spring Boot jar (see pom.xml), so the image always serves a fresh frontend.

# ---- Stage 1: build the jar (backend + frontend) ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY ["Taskly Backend/", "Taskly Backend/"]
COPY ["Taskly Frontend/", "Taskly Frontend/"]
# Caches keep downloaded Maven and npm packages between builds.
# Tests run in CI; skipping them here keeps image builds fast and database-free.
RUN --mount=type=cache,target=/root/.m2 \
    --mount=type=cache,target=/root/.npm \
    mvn -B -q -f "Taskly Backend/pom.xml" package -DskipTests

# ---- Stage 2: slim runtime image ----
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system taskly && useradd --system --gid taskly taskly
COPY --from=build ["/build/Taskly Backend/target/TMS-0.0.1-SNAPSHOT.jar", "app.jar"]
USER taskly
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
