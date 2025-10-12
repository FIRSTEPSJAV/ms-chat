# Use una imagen base que tenga Java instalado
FROM openjdk:17

# Copia el código fuente de la aplicación a la imagen
COPY . /app

# Establece el directorio de trabajo
WORKDIR /app

# Dar permisos de ejecución al wrapper de Maven
RUN chmod +x mvnw

# Construye el archivo JAR de la aplicación
RUN ./mvnw clean install -DskipTests

# Ejecuta la aplicación Spring Boot cuando se inicia el contenedor
CMD ["java", "-jar", "target/ms-chat-0.0.1-SNAPSHOT.jar"]


