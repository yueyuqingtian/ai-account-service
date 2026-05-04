FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY backend/pom.xml backend/maven-settings.xml ./
COPY backend/src ./src
RUN mvn -s maven-settings.xml -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/gpt-plus-service-*.jar /app/app.jar
COPY backend/render-start.sh /app/render-start.sh
RUN chmod +x /app/render-start.sh
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["/app/render-start.sh"]
