# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw mvnw
RUN chmod +x mvnw
RUN --mount=type=cache,target=/root/.m2 ./mvnw -DskipTests -Dcyclonedx.skip=true dependency:go-offline

COPY src src
RUN --mount=type=cache,target=/root/.m2 ./mvnw -T 1C -DskipTests -Dcyclonedx.skip=true package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/MedicalSystem-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV MONGODB_URI=mongodb://mongodb:27017/medicalsystem

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
