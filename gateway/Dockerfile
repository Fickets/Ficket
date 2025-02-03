FROM openjdk:17

WORKDIR /app

COPY build/libs/gateway-0.0.1-SNAPSHOT.jar gateway-0.0.1-SNAPSHOT.jar

EXPOSE 9000

# ENV SPRING_PROFILES_ACTIVE=prod

CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "gateway-0.0.1-SNAPSHOT.jar"]
