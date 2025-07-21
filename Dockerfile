FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

WORKDIR /app
COPY ./SberTorgiGovRu/pom.xml ./
COPY ./SberTorgiGovRu/src ./src
RUN mvn package

FROM eclipse-temurin:21-jre-jammy AS production
COPY --from=build /app/target/SberTorgiGovRu-*.jar /bot.jar

EXPOSE 8080
CMD ["java", "-jar", "bot.jar"]

