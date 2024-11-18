FROM openjdk:17

WORKDIR /app

COPY build/libs/config-0.0.1-SNAPSHOT.jar config-0.0.1-SNAPSHOT.jar

EXPOSE 8888

ENV SPRING_PROFILES_ACTIVE=server

CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "config-0.0.1-SNAPSHOT.jar"]
