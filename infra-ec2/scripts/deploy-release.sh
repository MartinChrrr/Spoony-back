#!/usr/bin/env bash
set -Eeuo pipefail
umask 077

if [[ $# -ne 8 ]]; then
  echo "Usage: deploy-release.sh RELEASE_DIR IMAGE_TAG REGION BUCKET PARAMETER_PREFIX API_DOMAIN ACME_EMAIL CORS_ORIGINS" >&2
  exit 64
fi

release_dir=$1
image_tag=$2
aws_region=$3
artifact_bucket=$4
parameter_prefix=$5
api_domain=$6
acme_email=$7
cors_origins=$8

exec 9>/run/lock/spoony-deploy.lock
flock -n 9 || { echo "Another Spoony deployment is already running" >&2; exit 75; }

cd /opt/spoony

parameter() {
  aws ssm get-parameter \
    --region "$aws_region" \
    --name "${parameter_prefix}/$1" \
    --with-decryption \
    --query 'Parameter.Value' \
    --output text
}

db_admin_password=$(parameter db-admin-password)
db_migration_password=$(parameter db-migration-password)
db_app_password=$(parameter db-app-password)
jwt_secret=$(parameter jwt-secret)

previous_dir=$(mktemp -d /opt/spoony/rollback.XXXXXX)
previous_image=""
if [[ -f .env ]]; then
  previous_image=$(sed -n "s/^APP_IMAGE='\(.*\)'$/\1/p" .env | head -n 1)
  cp -a .env docker-compose.production.yml Caddyfile "$previous_dir/" 2>/dev/null || true
fi

rollback() {
  exit_code=$?
  trap - ERR
  echo "Deployment failed; restoring the previous application release" >&2
  if [[ -n "$previous_image" && -f "$previous_dir/.env" ]]; then
    cp -a "$previous_dir/.env" "$previous_dir/docker-compose.production.yml" "$previous_dir/Caddyfile" /opt/spoony/
    docker compose -f /opt/spoony/docker-compose.production.yml up -d --remove-orphans db app caddy || true
  fi
  rm -rf "$previous_dir"
  exit "$exit_code"
}
trap rollback ERR

gzip -dc "$release_dir/backend-image.tar.gz" | docker load
docker image inspect "$image_tag" >/dev/null

install -m 0644 "$release_dir/docker-compose.production.yml" /opt/spoony/docker-compose.production.yml
install -m 0644 "$release_dir/Caddyfile" /opt/spoony/Caddyfile
install -m 0755 "$release_dir/backup-postgres.sh" /opt/spoony/backup-postgres.sh
install -m 0755 "$release_dir/rollback-release.sh" /opt/spoony/rollback-release.sh
install -m 0644 "$release_dir/spoony-backup.service" /etc/systemd/system/spoony-backup.service
install -m 0644 "$release_dir/spoony-backup.timer" /etc/systemd/system/spoony-backup.timer

cat >.env.tmp <<EOF
APP_IMAGE='$image_tag'
POSTGRES_DB='spoony'
DB_ADMIN_USER='spoony_admin'
DB_ADMIN_PASSWORD='$db_admin_password'
DB_MIGRATION_USER='spoony_migrator'
DB_MIGRATION_PASSWORD='$db_migration_password'
DB_APP_USER='spoony_app'
DB_APP_PASSWORD='$db_app_password'
JWT_SECRET='$jwt_secret'
CORS_ALLOWED_ORIGINS='$cors_origins'
API_DOMAIN='$api_domain'
ACME_EMAIL='$acme_email'
AWS_REGION='$aws_region'
ARTIFACT_BUCKET='$artifact_bucket'
EOF
chmod 0600 .env.tmp
mv .env.tmp .env

docker compose -f docker-compose.production.yml config --quiet
# A t4g.small has 2 GiB of RAM. Stop the previous web tier before running the
# migration JVM so the kernel cannot OOM-kill PostgreSQL during a deployment.
docker compose -f docker-compose.production.yml stop app caddy 2>/dev/null || true
docker compose -f docker-compose.production.yml up -d db

for _ in $(seq 1 30); do
  db_status=$(docker inspect --format '{{.State.Health.Status}}' spoony-db-1 2>/dev/null || true)
  [[ "$db_status" == "healthy" ]] && break
  sleep 5
done
[[ "${db_status:-}" == "healthy" ]]

docker compose -f docker-compose.production.yml --profile migration run --rm migration
docker compose -f docker-compose.production.yml up -d --remove-orphans db app caddy

for _ in $(seq 1 36); do
  app_status=$(docker inspect --format '{{.State.Health.Status}}' spoony-app-1 2>/dev/null || true)
  [[ "$app_status" == "healthy" ]] && break
  sleep 5
done
[[ "${app_status:-}" == "healthy" ]]

systemctl daemon-reload
systemctl enable spoony-docker.service
systemctl enable --now spoony-backup.timer

printf '%s\n' "$image_tag" > /opt/spoony/current-release

# Keep only the active and immediately previous backend images. This avoids
# touching unrelated Docker images while keeping rollback possible.
while IFS= read -r candidate_image; do
  [[ -z "$candidate_image" ]] && continue
  [[ "$candidate_image" == "$image_tag" ]] && continue
  [[ -n "$previous_image" && "$candidate_image" == "$previous_image" ]] && continue
  docker image rm "$candidate_image" >/dev/null 2>&1 || true
done < <(docker image ls spoony-backend --format '{{.Repository}}:{{.Tag}}')

trap - ERR
if [[ -n "$previous_image" && -f "$previous_dir/.env" ]]; then
  rm -rf /opt/spoony/previous
  mv "$previous_dir" /opt/spoony/previous
else
  rm -rf "$previous_dir"
fi
echo "Spoony release $image_tag is healthy"
