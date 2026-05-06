#!/bin/sh
# Render provides DATABASE_URL in postgres://user:pass@host:port/db format
# Spring Boot needs POSTGRES_JDBC_URL in jdbc:postgresql://host:port/db format
# This script converts DATABASE_URL to POSTGRES_JDBC_URL if not already set

if [ -n "$DATABASE_URL" ] && [ -z "$POSTGRES_JDBC_URL" ]; then
  # Parse postgres://user:password@host:port/database
  # Remove postgres:// prefix
  STRIPPED=$(echo "$DATABASE_URL" | sed 's|^postgres://||')
  # Extract user:password and host:port/database
  USER_PASS=$(echo "$STRIPPED" | cut -d'@' -f1)
  HOST_DB=$(echo "$STRIPPED" | cut -d'@' -f2-)
  # Extract username and password
  if [ -z "$POSTGRES_USERNAME" ]; then
    export POSTGRES_USERNAME=$(echo "$USER_PASS" | cut -d':' -f1)
  fi
  if [ -z "$POSTGRES_PASSWORD" ]; then
    export POSTGRES_PASSWORD=$(echo "$USER_PASS" | cut -d':' -f2-)
  fi
  # Build JDBC URL
  export POSTGRES_JDBC_URL="jdbc:postgresql://$HOST_DB"
  echo "Converted DATABASE_URL to POSTGRES_JDBC_URL=$POSTGRES_JDBC_URL"
fi

exec java -jar /app/app.jar
