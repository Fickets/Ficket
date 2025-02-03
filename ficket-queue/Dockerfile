FROM openjdk:17

WORKDIR /app

COPY build/libs/ficket-queue-0.0.1-SNAPSHOT.jar ficket-queue-0.0.1-SNAPSHOT.jar

EXPOSE 8089

ENV SPRING_PROFILES_ACTIVE=prod

CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "ficket-queue-0.0.1-SNAPSHOT.jar"]