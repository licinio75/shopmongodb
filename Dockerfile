# Dockerfile

# Paso 1: Usar una imagen base de Java
FROM openjdk:17-jdk-slim

# Paso 2: Establecer el directorio de trabajo en /app
WORKDIR /app

# Paso 3: Copiar el archivo JAR de la aplicación al contenedor
# Puedes reemplazar app.jar con el nombre específico de cada archivo JAR
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar

# Paso 4: Configurar variables de entorno (se pueden sobreescribir en Kubernetes)
ENV KAFKA_BROKER=kafka-service:9092
ENV MONGODB_URI="mongodb+srv://licinio:TQoGsw1E5drtIsid@clustershop.uekcp.mongodb.net/shop?retryWrites=true&w=majority&appName=ClusterShop"

# Paso 5: Exponer el puerto en el que la aplicación escucha (si es necesario)
# Para la API REST, puedes exponer el puerto 8080 o el puerto específico de cada servicio.
EXPOSE 8080

# Paso 6: Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar"]
