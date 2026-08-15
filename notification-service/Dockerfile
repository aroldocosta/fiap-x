FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY video-api-service/pom.xml video-api-service/pom.xml
COPY video-worker-service/pom.xml video-worker-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml

RUN mvn -pl common,notification-service -am dependency:go-offline

COPY common/src common/src
COPY notification-service/src notification-service/src

RUN mvn -pl common,notification-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

# Usando o curinga (*.jar) para garantir que pegamos o JAR executável gerado
COPY --from=build /workspace/notification-service/target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]