# Dockerfile
# 1) 커스텀 JRE 생성(glibc 계열로 통일)
FROM bellsoft/liberica-runtime-container:jdk-all-cds-slim AS builder-jre
RUN $JAVA_HOME/bin/jlink \
    --module-path "$JAVA_HOME/jmods" \
    --add-modules ALL-MODULE-PATH \
    --strip-debug \
    --compress=2 \
    --output /custom-jre

# 2) 애플리케이션 빌드
FROM gradle:9.0-jdk21-jammy AS builder

# 의존성 캐시 단계
COPY build.gradle .
COPY settings.gradle .
COPY gradle gradle/

RUN gradle dependencies

# 소스 빌드
COPY src ./src
RUN gradle bootJar --no-daemon

# 3) 실행 이미지
FROM alpine:3.22.1
ENV JAVA_HOME=/opt/java
ENV PATH="${JAVA_HOME}/bin:${PATH}"
ENV SPRING_PROFILES_ACTIVE=prod

COPY --from=builder-jre /custom-jre /opt/java

# Spring Boot 기본 설정상 단일 부트 JAR만 생성된다는 가정
COPY --from=builder /home/gradle/build/libs/*.jar /app.jar

EXPOSE 8080

ENTRYPOINT ["/opt/java/bin/java","-jar","/app.jar"]