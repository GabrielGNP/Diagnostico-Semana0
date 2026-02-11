FROM maven:3.9.12-eclipse-temurin-25-alpine AS builder
WORKDIR /build
COPY pom.xml ./
RUN mvn -B -f pom.xml dependency:go-offline

COPY . .
<<<<<<< HEAD
# Ejecutar tests primero - si fallan, la construcción se detiene
RUN mvn -B test
# Si los tests pasan, construir el proyecto
=======
>>>>>>> ab6c4255fa1a3e189b475473b95fdd5cdd95b37a
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:25-alpine
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]