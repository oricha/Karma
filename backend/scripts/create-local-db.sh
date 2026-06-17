#!/usr/bin/env bash
# Creates karma_local on PostgreSQL (default: localhost:5432).
# Override credentials: DB_HOST DB_PORT DB_USER DB_PASSWORD DB_NAME
set -euo pipefail

HOST="${DB_HOST:-localhost}"
PORT="${DB_PORT:-5432}"
USER="${DB_USER:-postgres}"
PASS="${DB_PASSWORD:-postgres}"
DB="${DB_NAME:-karma_local}"

export PGPASSWORD="$PASS"

echo "Connecting to PostgreSQL at ${HOST}:${PORT} as ${USER}..."

if ! psql -h "$HOST" -p "$PORT" -U "$USER" -d postgres -c "SELECT 1" >/dev/null 2>&1; then
  echo "ERROR: Cannot connect. Check that PostgreSQL is running and credentials are correct."
  echo "  export DB_PASSWORD=your_postgres_password"
  echo "  Or stop EnterpriseDB PG on :5432 and run: docker compose up -d postgres"
  exit 1
fi

EXISTS=$(psql -h "$HOST" -p "$PORT" -U "$USER" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = '${DB}'")
if [[ "$EXISTS" != "1" ]]; then
  echo "Creating database ${DB}..."
  psql -h "$HOST" -p "$PORT" -U "$USER" -d postgres -c "CREATE DATABASE ${DB} OWNER ${USER};"
else
  echo "Database ${DB} already exists."
fi

echo "Enabling PostGIS extension..."
psql -h "$HOST" -p "$PORT" -U "$USER" -d "$DB" -c "CREATE EXTENSION IF NOT EXISTS postgis;"

echo "Done. Start backend with:"
echo "  export SPRING_DATASOURCE_URL=jdbc:postgresql://${HOST}:${PORT}/${DB}"
echo "  export SPRING_DATASOURCE_USERNAME=${USER}"
echo "  export SPRING_DATASOURCE_PASSWORD=***"
echo "  cd backend && ./gradlew bootRun"
