#!/usr/bin/env bash
set -Eeuo pipefail
umask 077

cd /opt/spoony
set -a
# shellcheck disable=SC1091
source .env
set +a

timestamp=$(date -u +%Y%m%dT%H%M%SZ)
backup_file=$(mktemp "/srv/spoony/backups/spoony-${timestamp}.XXXXXX.dump")
trap 'rm -f "$backup_file"' EXIT

docker compose -f docker-compose.production.yml exec -T \
  -e PGPASSWORD="$DB_ADMIN_PASSWORD" db \
  pg_dump --username "$DB_ADMIN_USER" --dbname "$POSTGRES_DB" \
    --format=custom --compress=6 --no-owner --no-acl > "$backup_file"

test -s "$backup_file"
object_key="backups/database/${timestamp}.dump"
aws s3 cp "$backup_file" "s3://${ARTIFACT_BUCKET}/${object_key}" \
  --region "$AWS_REGION" \
  --only-show-errors \
  --sse AES256
aws s3api head-object \
  --bucket "$ARTIFACT_BUCKET" \
  --key "$object_key" \
  --region "$AWS_REGION" >/dev/null

echo "Database backup uploaded to s3://${ARTIFACT_BUCKET}/${object_key}"
