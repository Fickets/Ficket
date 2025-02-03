FROM openjdk:17

WORKDIR /app

COPY build/libs/gateway-0.0.1-SNAPSHOT.jar gateway-0.0.1-SNAPSHOT.jar

EXPOSE 9000

ENV SPRING_APPLICATION_NAME=apigateway-service
ENV SPRING_PROFILES_ACTIVE=prod
ENV SPRING_CONFIG_IMPORT=optional:configserver:http://config-server:8888

CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "gateway-0.0.1-SNAPSHOT.jar",
     "--spring.application.name=${SPRING_APPLICATION_NAME}",
     "--spring.profiles.active=${SPRING_PROFILES_ACTIVE}",
     "--spring.config.import=${SPRING_CONFIG_IMPORT}"]
