FROM eclipse-temurin:21-jdk-jre-slim

WORKDIR /app

COPY target/*.jar app.jar

ENV PORT=8080

EXPOSE $PORT

ENTRYPOINT ["java", "-jar", "app.jar"]