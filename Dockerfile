# syntax=docker/dockerfile:1
#
# Il Magro en una sola imagen: el backend Spring Boot sirve tambien el front Angular.
# Se construye en tres etapas y la imagen final solo lleva el JRE y el jar (sin Node, Maven ni codigo).
#   docker build -t ilmagro .
#   docker run --rm -p 8080:8080 --env-file backend/.env ilmagro

# ---------- 1) Front: compila Angular ----------
FROM node:22-alpine AS front
WORKDIR /front
# primero solo los manifiestos: si no cambian las dependencias, esta capa sale de la cache
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npx ng build --configuration production
# queda en /front/dist/frontend/browser

# ---------- 2) Back: compila el jar con el front adentro ----------
FROM maven:3.9-eclipse-temurin-21 AS back
WORKDIR /back
COPY backend/pom.xml ./
# baja las dependencias en su propia capa (cache mientras no cambie el pom)
RUN mvn -B -q dependency:go-offline
COPY backend/src ./src
# Spring Boot sirve lo que hay en classpath:/static
COPY --from=front /front/dist/frontend/browser/ ./src/main/resources/static/
# los tests corren aparte (CI o local): aca solo se empaqueta
RUN mvn -B -q package -DskipTests

# ---------- 3) Imagen final ----------
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=back /back/target/barberia-*.jar app.jar
USER app

# Pensado para un plan chico (~512 MB): recolector simple, compilacion rapida de arranque,
# heap acotado y que el proceso muera si se queda sin memoria (la plataforma lo reinicia)
ENV JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xmx320m -Xss512k -XX:MaxMetaspaceSize=128m -XX:+ExitOnOutOfMemoryError"

# El puerto lo da la plataforma en PORT (Render); sin eso, 8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
