# ── Stage 1: Build ──────────────────────────────────────────
FROM amazoncorretto:21 AS builder

WORKDIR /app
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties* ./
COPY gradle/ gradle/
COPY shared/ shared/
COPY service/ service/

RUN chmod +x gradlew && ./gradlew clean build -x test --no-daemon --parallel

# ── Stage 2: Runtime ────────────────────────────────────────
FROM amazoncorretto:21-alpine

ARG SERVICE_NAME
ENV SERVICE_NAME=${SERVICE_NAME}

WORKDIR /app
COPY --from=builder /app/service/${SERVICE_NAME}/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
