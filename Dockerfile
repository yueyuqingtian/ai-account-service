FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY backend/pom.xml backend/maven-settings.xml ./
COPY backend/src ./src
RUN mvn -s maven-settings.xml -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/gpt-plus-service-*.jar /app/app.jar
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["/bin/sh", "-c", "\
if [ -n \"$DATABASE_URL\" ] && [ -z \"$POSTGRES_JDBC_URL\" ]; then \
  STRIPPED=$(echo \"$DATABASE_URL\" | sed 's|^postgres://||'); \
  HOST_DB=$(echo \"$STRIPPED\" | cut -d'@' -f2-); \
  export POSTGRES_JDBC_URL=\"jdbc:postgresql://$HOST_DB\"; \
  echo \"Converted DATABASE_URL to POSTGRES_JDBC_URL=$POSTGRES_JDBC_URL\"; \
fi; \
exec java -jar /app/app.jar\
"]
