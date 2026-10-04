#!/usr/bin/env bash
set -Eeuo pipefail

exec 9>/run/lock/spoony-deploy.lock
flock -n 9 || { echo "Another Spoony deployment is already running" >&2; exit 75; }

if [[ ! -f /opt/spoony/previous/.env ]]; then
  echo "No previous release is available" >&2
  exit 1
fi

cd /opt/spoony
failed_dir=$(mktemp -d /opt/spoony/failed.XXXXXX)
cp -a .env docker-compose.production.yml Caddyfile "$failed_dir/"
cp -a previous/.env previous/docker-compose.production.yml previous/Caddyfile /opt/spoony/

docker compose -f docker-compose.production.yml config --quiet
docker compose -f docker-compose.production.yml up -d --remove-orphans db app caddy

for _ in $(seq 1 36); do
  app_status=$(docker inspect --format '{{.State.Health.Status}}' spoony-app-1 2>/dev/null || true)
  [[ "$app_status" == "healthy" ]] && break
  sleep 5
done
[[ "${app_status:-}" == "healthy" ]]

rm -rf previous
mv "$failed_dir" previous
sed -n "s/^APP_IMAGE='\(.*\)'$/\1/p" .env > current-release
echo "Previous application image restored; database migrations were not reverted"
