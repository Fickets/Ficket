# 기본 Java 17 이미지를 사용
FROM openjdk:17-jdk-slim

# 작업 디렉터리 설정
WORKDIR /app

COPY build/libs/gateway-0.0.1-SNAPSHOT.jar gateway-0.0.1-SNAPSHOT.jar

# 애플리케이션 실행 포트
EXPOSE 9000

# 환경 변수 설정
ENV SPRING_APPLICATION_NAME=apigateway-service \
    SPRING_PROFILES_ACTIVE=prod \
    SPRING_CONFIG_IMPORT=optional:configserver:http://config-server:8888

# 애플리케이션 실행
CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "gateway-0.0.1-SNAPSHOT.jar"]
