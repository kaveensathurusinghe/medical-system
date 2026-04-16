FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy pre-built jar (build with `mvn -f auth-service/pom.xml -DskipTests package`)
COPY auth-service/target/auth-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
