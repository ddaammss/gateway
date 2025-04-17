# base 이미지 설정
FROM openjdk:17-jdk-slim

# 빌드 시 사용할 ARG (빌드 시점의 변수)
ARG APP_NAME
ARG SPRING_PROFILE

# 런타임 환경 변수 설정
ENV APP_NAME=${APP_NAME}
ENV SPRING_PROFILE=${SPRING_PROFILE}
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 -Dspring.profiles.active=${SPRING_PROFILE}"

# 작업 디렉토리 생성
WORKDIR /app

# Create logs directory with correct permissions
RUN mkdir -p /app/logs && chmod 777 /app/logs

# 빌드된 JAR 파일을 컨테이너로 복사
RUN ls -l ./
COPY ./${APP_NAME}.jar /app/${APP_NAME}.jar
RUN ls -l /app/

# 로그 디렉토리 생성
RUN mkdir -p /app/logs

# 애플리케이션 포트 노출
EXPOSE ${APP_PORT}

RUN printenv

# 실행 명령어
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/${APP_NAME}.jar"]
