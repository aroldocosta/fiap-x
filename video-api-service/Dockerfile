FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY video-api-service/pom.xml video-api-service/pom.xml
COPY video-worker-service/pom.xml video-worker-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml

RUN mvn -pl common,video-api-service -am dependency:go-offline

COPY common/src common/src
COPY video-api-service/src video-api-service/src

RUN mvn -pl common,video-api-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

# Usa o curinga e exclui arquivos .original caso existam
COPY --from=build /workspace/video-api-service/target/video-api-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]