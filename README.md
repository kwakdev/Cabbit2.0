# Cabbit Field Service

Cabbit is a mobile-first, offline-capable field-service operations product. Managers schedule and assign work; field teams complete checklists, capture photos, record materials and equipment, track time, report incidents, and synchronize when connectivity returns.

## Repository map

- `api/` - Java 21 Spring Boot REST API, JWT authentication, SQL Server migrations, OpenAPI and health endpoints
- `mobile/` - Expo SDK 57 React Native application with role-aware navigation, encrypted tokens, SQLite cache and an offline mutation queue
- `infra/` - production/staging Docker Compose, Caddy, Prometheus, Grafana, backup and deployment scripts
- `docs/` - architecture, operations, security, product decisions, user guides and backlog traceability

## Fast local start

Requirements: Java 21+, Maven 3.9+, Node 22+, Docker Engine with Compose.

```bash
cp .env.example .env
# edit .env; do not keep CHANGE_ME values
docker compose -f infra/compose.yml --env-file .env up -d --build
cd mobile
npm install
npx expo start
```

The API is available through Caddy at `https://fieldservice.localhost` (accept the local CA during development), directly at `http://localhost:8080`, and Swagger UI is at `http://localhost:8080/swagger-ui.html`. The seeded local administrator is `admin@cabbit.local` / `ChangeMe123!`; the API forces a password change flag and production startup refuses this default.

## Development without Docker

```bash
cd api
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

```bash
cd mobile
npm install
npm run start
```

Run checks with `mvn verify` in `api/` and `npm test -- --runInBand && npm run typecheck` in `mobile/`.

## Production

Follow [`docs/PROXMOX_DEPLOYMENT.md`](docs/PROXMOX_DEPLOYMENT.md). It covers both the recommended Ubuntu VM and a privileged/nesting-enabled LXC, host sizing, networking, hardening, Tailscale, installation, backups, automated deployment, validation and rollback.

## Product scope

The executable MVP backlog is represented in code, tests, migrations, Compose and runbooks. Tickets requiring a named pilot, interviews, app-store accounts, DNS, physical backup media, UPS hardware or approval by real people remain operator gates and are explicitly identified in [`docs/BACKLOG_TRACEABILITY.md`](docs/BACKLOG_TRACEABILITY.md).

