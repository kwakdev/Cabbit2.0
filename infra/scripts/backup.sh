#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
set -a; source .env; set +a
reason="${1:-nightly}"; stamp="$(date -u +%Y%m%dT%H%M%SZ)"; out="backups/${stamp}-${reason}"
mkdir -p "$out"
docker compose --env-file .env exec -T sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$SQLSERVER_PASSWORD" -C -Q "BACKUP DATABASE [${SQLSERVER_DATABASE}] TO DISK='/var/opt/mssql/backups/${stamp}.bak' WITH CHECKSUM, COMPRESSION, INIT"
docker run --rm -v cabbit-field-service_backups:/source:ro -v "$(pwd)/$out:/dest" alpine:3.22 cp "/source/${stamp}.bak" /dest/database.bak
docker run --rm -v cabbit-field-service_minio_data:/source:ro -v "$(pwd)/$out:/dest" alpine:3.22 tar -C /source -czf /dest/minio.tar.gz .
cp compose.yml Caddyfile "$out/"
openssl enc -aes-256-cbc -salt -pbkdf2 -in "$out/database.bak" -out "$out/database.bak.enc" -pass env:BACKUP_ENCRYPTION_PASSPHRASE
openssl enc -aes-256-cbc -salt -pbkdf2 -in "$out/minio.tar.gz" -out "$out/minio.tar.gz.enc" -pass env:BACKUP_ENCRYPTION_PASSPHRASE
rm "$out/database.bak" "$out/minio.tar.gz"
sha256sum "$out"/* > "$out/SHA256SUMS"
find backups -mindepth 1 -maxdepth 1 -type d -mtime +35 -exec rm -rf -- {} +
echo "Encrypted backup created: $out (copy it off-host now)"

