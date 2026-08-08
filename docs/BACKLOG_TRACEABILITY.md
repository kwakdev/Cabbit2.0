# Backlog traceability

The source backlog mixes software deliverables, organization setup, real-world discovery and operational acceptance. This repository completes the buildable MVP as grouped below; fine-grained IDs are retained in code/test/runbook headings where they add operational value.

| Backlog groups | Evidence |
|---|---|
| DISC, PM, REPO, ARCH | `docs/PRODUCT.md`, this file, ADR content in `docs/ARCHITECTURE.md`, templates and workflows |
| PROX, DOCKER, NET | `docs/PROXMOX_DEPLOYMENT.md`, `infra/compose.yml`, Caddy and hardening scripts |
| DB, API | `api/src/main/resources/db/migration`, API source, OpenAPI/Actuator configuration |
| MOB, AUTH | `mobile/`, secure token store, role-aware UI; API security/auth modules |
| USER, CUST, WORK, SCHED | resource endpoints, domain migration and manager/worker screens |
| CHECK, PHOTO, MAT, EQUIP, TIME, ISSUE | operations endpoints, private MinIO flow, offline-capable field execution UI |
| OFF, SYNC | SQLite cache/operation queue, idempotent batch sync and conflict results |
| NOTIF, REPORT, SEARCH, AUDIT | notification, dashboard/search/report/export and immutable audit APIs/screens |
| SEC, TEST | threat model, validation/RBAC/tenant protections and automated test suites |
| CICD, BUILD | `.github/workflows`, EAS profiles, deploy/rollback scripts |
| MON, BACK | Prometheus/Grafana/Uptime Kuma services, backup/verify/restore runbooks |
| DOC | repository guides, API Swagger UI, schema/migration and user/operations guides |

## Operator-gated tickets

These cannot honestly be completed in source control: selecting and interviewing a real pilot company/users; recording physical server/UPS specifications; creating Proxmox guests and DNS/Tailscale policy; provisioning app-store, EAS, GitHub and alert-provider credentials; performing device, restore, outage and pilot acceptance exercises; production imports/training; and one-week/one-month reviews. Each has a checklist in the deployment and operations guides. Record names, dates, evidence and sign-off before marking them done in Asana.

## Definition of done

Code tickets require implementation, automated checks, updated API/migration documentation, offline/security review and a passing deployment smoke test. Operational tickets require dated evidence and named approval; a written procedure alone is not counted as execution.

