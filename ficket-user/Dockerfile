# 기본 Java 17 이미지를 사용
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/ficket-user-0.0.1-SNAPSHOT.jar ficket-user-0.0.1-SNAPSHOT.jar

EXPOSE 8089

# 환경 변수 설정
ENV SPRING_APPLICATION_NAME=user-service \
    SPRING_PROFILES_ACTIVE=prod \
    SPRING_CONFIG_IMPORT=optional:configserver:http://config-server:8888

# 애플리케이션 실행
CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
