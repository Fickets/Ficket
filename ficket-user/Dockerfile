FROM openjdk:17

WORKDIR /app

COPY build/libs/ficket-user-0.0.1-SNAPSHOT.jar ficket-user-0.0.1-SNAPSHOT.jar

EXPOSE 8089

ENV SPRING_APPLICATION_NAME=user-service
ENV SPRING_PROFILES_ACTIVE=prod
ENV SPRING_CONFIG_IMPORT=optional:configserver:http://config-server:8888


CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "ficket-user-0.0.1-SNAPSHOT.jar",
     "--spring.application.name=${SPRING_APPLICATION_NAME}",
     "--spring.profiles.active=${SPRING_PROFILES_ACTIVE}",
     "--spring.config.import=${SPRING_CONFIG_IMPORT}"]