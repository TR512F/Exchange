FROM eclipse-temurin:22-jdk-alpine

ARG APP_JAR=/target/*.jar

COPY ${APP_JAR} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]