# Base Image
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# Elasticsearch CA 인증서 복사
COPY esCert/elasticsearch.crt /usr/local/share/ca-certificates/ca.crt

# Java TrustStore에 `ca.crt` 등록
RUN keytool -import -trustcacerts -keystore /usr/local/openjdk-17/lib/security/cacerts \
    -storepass changeit -noprompt -alias elasticsearch-root-ca \
    -file /usr/local/share/ca-certificates/ca.crt

# JAR 파일 복사
COPY Ficket/ficket-search/build/libs/ficket-search-0.0.1-SNAPSHOT.jar app.jar

# 애플리케이션 실행 포트
EXPOSE 8094

# 애플리케이션 실행
CMD ["java", "-Duser.timezone=Asia/Seoul", \
     "-Dspring.application.name=search-service", \
     "-Dspring.profiles.active=prod", \
     "-Dspring.config.import=optional:configserver:http://config-server:8888", \
     "-Djavax.net.ssl.trustStore=/usr/local/openjdk-17/lib/security/cacerts", \
     "-Djavax.net.ssl.trustStorePassword=changeit", \
     "-jar", "app.jar"]
