#!/usr/bin/env bash
set -euo pipefail
base_url="${1:-https://localhost}"
curl --fail --silent --show-error --retry 10 --retry-delay 3 --insecure "${base_url}/actuator/health/readiness" | grep -q '"status":"UP"'
echo "Cabbit readiness check passed: ${base_url}"

