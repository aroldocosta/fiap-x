FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY video-api-service/pom.xml video-api-service/pom.xml
COPY video-worker-service/pom.xml video-worker-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml

RUN mvn -pl auth-service -am dependency:go-offline

COPY auth-service/src auth-service/src

RUN mvn -pl auth-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

# Usa o curinga (*.jar) para garantir que pega o JAR executável correto
COPY --from=build /workspace/auth-service/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]