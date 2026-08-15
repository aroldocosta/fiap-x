FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY video-api-service/pom.xml video-api-service/pom.xml
COPY video-worker-service/pom.xml video-worker-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml

RUN mvn -pl common,video-worker-service -am dependency:go-offline

COPY common/src common/src
COPY video-worker-service/src video-worker-service/src

RUN mvn -pl common,video-worker-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

# Instala o FFmpeg no container de execução limpo
RUN apt-get update \
    && apt-get install -y --no-install-recommends ffmpeg \
    && rm -rf /var/lib/apt/lists/*

# Usa o curinga (*.jar) para evitar falhas de nome
COPY --from=build /workspace/video-worker-service/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]