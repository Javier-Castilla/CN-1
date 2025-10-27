FROM eclipse-temurin:20-jre-jammy

WORKDIR /app

COPY target/*.jar app.jar

ENV SERVER_PORT=8082
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]