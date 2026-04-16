# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -DskipTests dependency:go-offline

COPY src src
RUN --mount=type=cache,target=/root/.m2 mvn -T 1C -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/appointments-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8083

ENV MONGODB_URI=mongodb://mongodb:27017/medicalsystem

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
