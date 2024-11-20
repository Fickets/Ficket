FROM openjdk:17

WORKDIR /app

COPY build/libs/discovery-0.0.1-SNAPSHOT.jar discovery-0.0.1-SNAPSHOT.jar

EXPOSE 8761

CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "discovery-0.0.1-SNAPSHOT.jar"]
