# Operations and user guide

Managers create customers and locations, create a work order with title/status/schedule/instructions, assign employees, and monitor dashboard/incident/time-approval queues. Workers use Today, start work, complete the record, attach photos, report incidents when needed, stop time, and tap the sync card when connectivity returns.

Before pilot: create the pilot company, users, customers, locations, checklist templates, materials and equipment; install the signed staging build; connect devices through Tailscale; run one connected and one fully offline work order; verify before/after photos, time, incident escalation and weekly export. Record sync conflicts and resolve critical defects before approval.

Troubleshooting: an offline banner is expected without connectivity. Confirm Tailscale, API URL and device clock; tap sync and inspect pending count. For API deployment failures, inspect `docker compose logs api`, SQL/MinIO health and Flyway errors, then use rollback. For SQL issues, check volume space, memory and login failures; do not delete the volume. For photo issues, verify the private bucket exists and server time is accurate because signed URLs expire.

Launch checklist: verified off-host backup and restore evidence; HTTPS; firewall/ACL; rotated secrets; no public SQL/MinIO/monitoring; alerts tested; automatic deploy and rollback tested; production administrator changed password; employee/customer/location imports reviewed; signed mobile build installed; users trained; first jobs and first weekly report observed. Repeat review after one week and one month.

