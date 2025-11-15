# Multi-stage build
# Etapa 1: Build de la aplicación
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copia archivos de configuración de Maven
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

# Da permisos de ejecución al wrapper de Maven
RUN chmod +x mvnw

# Descarga dependencias
RUN ./mvnw dependency:go-offline -B

# Copia el código fuente
COPY src/ src/

# Compila la aplicación
RUN ./mvnw clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copia el JAR compilado desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Expone el puerto donde correrá tu app
EXPOSE 8080

# Comando para ejecutar tu aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
