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

# glibc 설치 (Infisical CLI 실행을 위해 필요)
RUN apk add --no-cache gcompat bash curl

# Infisical CLI 설치
RUN curl -1sLf 'https://dl.cloudsmith.io/public/infisical/infisical-cli/setup.alpine.sh' | bash \
    && apk add --no-cache infisical

COPY --from=builder-jre /custom-jre /opt/java

# Spring Boot 기본 설정상 단일 부트 JAR만 생성된다는 가정
COPY --from=builder /home/gradle/build/libs/*.jar /app.jar

EXPOSE 8080

# 엔트리포인트: 비대화형 로그인 → 주입 → 실행
RUN printf '%s\n' '#!/bin/sh -e' \
  'if [ -z "$INFISICAL_API_URL" ]; then' \
  '  echo "Set INFISICAL_API_URL (예: https://infisical.example.com/api)"; exit 1; fi' \
  'export INFISICAL_DISABLE_UPDATE_CHECK=true' \
  'if [ -z "$INFISICAL_TOKEN" ] && [ -n "$INFISICAL_CLIENT_ID" ] && [ -n "$INFISICAL_CLIENT_SECRET" ]; then' \
  '  INFISICAL_TOKEN=$(infisical login --method=universal-auth --client-id="$INFISICAL_CLIENT_ID" --client-secret="$INFISICAL_CLIENT_SECRET" --plain --silent); export INFISICAL_TOKEN;' \
  'fi' \
  'exec infisical run --projectId="${INFISICAL_PROJECT_ID:?Set INFISICAL_PROJECT_ID}" --path="${INFISICAL_PATH}" --domain "${INFISICAL_API_URL}" -- java -jar /app.jar' \
  > /usr/local/bin/entrypoint.sh \
 && chmod +x /usr/local/bin/entrypoint.sh

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]