#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
test -f .env || { echo "infra/.env is missing" >&2; exit 1; }
set -a; source .env; set +a
previous="$(docker compose --env-file .env images -q api 2>/dev/null || true)"
printf '%s' "$previous" > .previous-api-image
./scripts/backup.sh pre-deploy
docker compose --env-file .env pull api
docker compose --env-file .env up -d --remove-orphans
if ! ./scripts/smoke.sh "https://${APP_HOSTNAME:-localhost}"; then
  ./scripts/rollback.sh
  exit 1
fi
date -u +%FT%TZ > .last-successful-deploy
