FROM maven:3.9.12-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml ./
RUN mvn -B -f pom.xml dependency:go-offline

COPY . .
# Construir el proyecto sin ejecutar tests durante la fase de build
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
