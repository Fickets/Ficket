# 기본 Java 17 이미지를 사용
FROM openjdk:17-jdk-slim

# 작업 디렉터리 설정
WORKDIR /app

# JAR 파일 복사
COPY build/libs/ficket-search-0.0.1-SNAPSHOT.jar app.jar

# 애플리케이션 실행 포트
EXPOSE 8091

# 환경 변수 설정
ENV SPRING_APPLICATION_NAME=search-service \
    SPRING_PROFILES_ACTIVE=prod \
    SPRING_CONFIG_IMPORT=optional:configserver:http://config-server:8888

# 애플리케이션 실행
CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
