# Imagen del servidor Mi Estanteria para la nube (Railway u otro proveedor con Docker).
# 1) Compila el proyecto con Gradle  2) Ejecuta solo el .jar en una imagen liviana de Java 21.

FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew --no-daemon -q dependencies > /dev/null 2>&1 || true
COPY src src
RUN ./gradlew --no-daemon -q bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/mi-estanteria-backend-1.0.0.jar app.jar
# Railway asigna el puerto en la variable PORT
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx384m", "-jar", "app.jar"]
