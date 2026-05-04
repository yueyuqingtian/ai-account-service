FROM node:20-alpine AS frontend-build
WORKDIR /workspace
ENV VITE_API_BASE_URL=
COPY package.json package-lock.json ./
COPY apps ./apps
COPY packages ./packages
RUN npm ci
RUN npm run build:web
RUN npm run build:admin

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY backend/pom.xml backend/maven-settings.xml ./
COPY backend/src ./src
COPY --from=frontend-build /workspace/apps/web/dist ./src/main/resources/static
COPY --from=frontend-build /workspace/apps/admin/dist ./src/main/resources/static/admin-ui
RUN mvn -s maven-settings.xml -Dmaven.test.skip=true package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/gpt-plus-service-*.jar /app/app.jar
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
