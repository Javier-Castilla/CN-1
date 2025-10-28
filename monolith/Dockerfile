FROM eclipse-temurin:20-jre-jammy

WORKDIR /app

COPY target/*-SNAPSHOT.jar app.jar

ENV SERVER_PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]