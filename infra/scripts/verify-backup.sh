#!/usr/bin/env bash
set -euo pipefail
backup="${1:?usage: verify-backup.sh BACKUP_DIRECTORY}"
cd "$backup"
sha256sum --check SHA256SUMS
test -s database.bak.enc && test -s minio.tar.gz.enc
echo "Backup checksums and required files are valid. A quarterly restore test is still required."

