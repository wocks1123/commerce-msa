# ── Stage 1: Build ──────────────────────────────────────────
FROM amazoncorretto:21 AS builder

# gradlew(Gradle 8.9)가 JVM 인자 파싱에 xargs를 요구하는데 베이스 이미지에 없다.
# 없으면 "xargs is not available" 로 빌드가 즉시 중단된다.
RUN yum install -y findutils && yum clean all

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
