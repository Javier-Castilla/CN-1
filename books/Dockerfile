FROM eclipse-temurin:20-jre-jammy

WORKDIR /app

COPY target/*.jar app.jar

ENV SERVER_PORT=8081
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]