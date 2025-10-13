# -----Build-----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Gradle 캐시 최적화를 위해 먼저 빌드 스크립트만 복사
COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle* build.gradle* ./
RUN chmod +x gradlew

# 의존성만 먼저 당겨서 캐시
RUN ./gradlew dependencies --no-daemon || true

# 실제 소스 복사 후 빌드
COPY src src
RUN ./gradlew clean bootjar -x test --no-daemon

# -----Run-----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-Xms128m", "-Xmx384m", "-jar", "/app/app/jar"]
