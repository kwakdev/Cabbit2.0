#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
set -a; source .env; set +a
test -s .previous-api-image || { echo "No previous image recorded" >&2; exit 1; }
previous="$(cat .previous-api-image)"
docker tag "$previous" "${API_IMAGE:-cabbit-field-service-api}:rollback"
APP_VERSION=rollback docker compose --env-file .env up -d api caddy
./scripts/smoke.sh "https://${APP_HOSTNAME:-localhost}"
