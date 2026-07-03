# Stage 1: Build frontend
FROM node:22-alpine AS frontend-build
WORKDIR /frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ .
RUN npx vite build

# Stage 2: Build backend
FROM eclipse-temurin:21-jdk-alpine AS backend-build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
COPY --from=frontend-build /frontend/dist ./src/main/resources/static
RUN apk add --no-cache maven && mvn package -DskipTests -q

# Stage 3: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
