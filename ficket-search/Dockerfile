# 기본 Java 17 이미지를 사용
FROM openjdk:17-jdk-slim

# 작업 디렉터리 설정
WORKDIR /app

# JAR 파일 복사
COPY build/libs/ficket-search-0.0.1-SNAPSHOT.jar app.jar

# Elasticsearch CA 인증서 복사
COPY certs/ca.crt /usr/local/share/ca-certificates/ca.crt

# Java TrustStore에 `ca.crt` 등록
RUN keytool -import -trustcacerts -keystore $JAVA_HOME/lib/security/cacerts \
    -storepass changeit -noprompt -alias elasticsearch-root-ca \
    -file /usr/local/share/ca-certificates/ca.crt

# 애플리케이션 실행 포트
EXPOSE 8094

# 환경 변수 설정
ENV SPRING_APPLICATION_NAME=search-service \
    SPRING_PROFILES_ACTIVE=prod \
    SPRING_CONFIG_IMPORT=optional:configserver:http://config-server:8888 \
    JAVA_TRUSTSTORE_PATH=$JAVA_HOME/lib/security/cacerts \
    JAVA_TRUSTSTORE_PASSWORD=changeit

# 애플리케이션 실행 (TrustStore 적용)
CMD ["java", "-Duser.timezone=Asia/Seoul", \
     "-Dspring.application.name=${SPRING_APPLICATION_NAME}", \
     "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", \
     "-Dspring.config.import=${SPRING_CONFIG_IMPORT}", \
     "-Djavax.net.ssl.trustStore=${JAVA_TRUSTSTORE_PATH}", \
     "-Djavax.net.ssl.trustStorePassword=${JAVA_TRUSTSTORE_PASSWORD}", \
     "-jar", "app.jar"]