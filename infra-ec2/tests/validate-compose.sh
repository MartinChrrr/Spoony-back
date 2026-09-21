#!/usr/bin/env bash
set -Eeuo pipefail

project_name=spoony-validation
compose_files=(
  -p "$project_name"
  -f infra-ec2/docker-compose.production.yml
  -f infra-ec2/tests/docker-compose.test.yml
)

cleanup() {
  docker compose "${compose_files[@]}" down --volumes --remove-orphans >/dev/null 2>&1 || true
}
trap cleanup EXIT

export APP_IMAGE=spoony-backend:compose-validation
export POSTGRES_DB=spoony
export DB_ADMIN_USER=spoony_admin
export DB_ADMIN_PASSWORD=validation-admin-password
export DB_MIGRATION_USER=spoony_migrator
export DB_MIGRATION_PASSWORD=validation-migration-password
export DB_APP_USER=spoony_app
export DB_APP_PASSWORD=validation-app-password
export JWT_SECRET=validation-jwt-secret-with-at-least-thirty-two-characters
export CORS_ALLOWED_ORIGINS=https://example.test
export API_DOMAIN=api.example.test
export ACME_EMAIL=ops@example.test
export AWS_REGION=eu-west-3
export ARTIFACT_BUCKET=validation-bucket

cleanup
docker compose "${compose_files[@]}" config --quiet
docker compose "${compose_files[@]}" up -d db
docker compose "${compose_files[@]}" --profile migration run --rm migration
docker compose "${compose_files[@]}" up -d app

for _ in $(seq 1 36); do
  if curl --fail --silent http://127.0.0.1:18080/actuator/health/readiness >/dev/null; then
    break
  fi
  sleep 5
done
curl --fail --silent http://127.0.0.1:18080/actuator/health/readiness | grep -q '"status":"UP"'

curl --fail --silent \
  --request POST \
  --header 'Content-Type: application/json' \
  --data '{"email":"infra-validation@example.test","password":"validation-password","firstName":"Infra","consentGiven":true}' \
  http://127.0.0.1:18080/api/v1/auth/register | grep -q '"status":"success"'

runtime_count=$(docker compose "${compose_files[@]}" exec -T \
  -e PGPASSWORD="$DB_APP_PASSWORD" db \
  psql -h db -U "$DB_APP_USER" -d "$POSTGRES_DB" -Atc 'select count(*) from users')
[[ "$runtime_count" == "1" ]]

if docker compose "${compose_files[@]}" exec -T \
  -e PGPASSWORD="$DB_APP_PASSWORD" db \
  psql -h db -U "$DB_APP_USER" -d "$POSTGRES_DB" \
  -c 'select * from flyway_schema_history' >/dev/null 2>&1; then
  echo "Runtime database role unexpectedly reads Flyway history" >&2
  exit 1
fi

app_user=$(docker compose "${compose_files[@]}" exec -T app id -un)
[[ "$app_user" == "spoony" ]]

if docker compose "${compose_files[@]}" exec -T app touch /root-filesystem-probe >/dev/null 2>&1; then
  echo "Application root filesystem unexpectedly accepts writes" >&2
  exit 1
fi

db_ports=$(docker port "${project_name}-db-1")
[[ -z "$db_ports" ]]

backup_file=$(mktemp /tmp/spoony-compose-validation.XXXXXX.dump)
trap 'rm -f "$backup_file"; cleanup' EXIT
docker compose "${compose_files[@]}" exec -T \
  -e PGPASSWORD="$DB_ADMIN_PASSWORD" db \
  pg_dump -h db -U "$DB_ADMIN_USER" -d "$POSTGRES_DB" \
    --format=custom --compress=6 --no-owner --no-acl > "$backup_file"
test -s "$backup_file"

docker compose "${compose_files[@]}" exec -T \
  -e PGPASSWORD="$DB_ADMIN_PASSWORD" db \
  createdb -h db -U "$DB_ADMIN_USER" --owner="$DB_MIGRATION_USER" spoony_restore
docker compose "${compose_files[@]}" exec -T \
  -e PGPASSWORD="$DB_MIGRATION_PASSWORD" db \
  pg_restore -h db -U "$DB_MIGRATION_USER" -d spoony_restore \
    --no-owner --no-acl < "$backup_file"
docker compose "${compose_files[@]}" --profile migration run --rm \
  -e DATABASE_URL=jdbc:postgresql://db:5432/spoony_restore migration

restored_count=$(docker compose "${compose_files[@]}" exec -T \
  -e PGPASSWORD="$DB_APP_PASSWORD" db \
  psql -h db -U "$DB_APP_USER" -d spoony_restore -Atc 'select count(*) from users')
[[ "$restored_count" == "1" ]]

echo "Production Compose validation passed"
