# Imagen base con JDK 17 (compatible con Spring Boot)
FROM openjdk:17-jdk-slim

# Crea una carpeta en el contenedor
WORKDIR /app

# Copia el archivo .jar generado por Maven
# (usa un comodín por si el nombre del jar cambia con el tiempo)
COPY target/*.jar app.jar

# Expone el puerto donde correrá tu app (Render usa variable PORT)
EXPOSE 8080

# Comando para ejecutar tu aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
