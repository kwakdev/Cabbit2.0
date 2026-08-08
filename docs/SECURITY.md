# Security and threat model

Primary threats are credential theft, lost devices, cross-company data access, malicious uploads, exposed infrastructure, vulnerable dependencies and destructive runner access.

Controls include short-lived HMAC JWT access tokens, rotating hashed refresh tokens, BCrypt passwords, login throttling/lockout, tenant IDs derived from the token, method/endpoint RBAC, strict validation, private object storage, MIME/size checks, random object keys, HTTPS, security headers, internal-only SQL/MinIO networks, non-root containers, read-only filesystems where practical, dependency/container/secret scanning, encrypted tokens and SQLite, remote session revocation, audit events and encrypted off-host backups.

Lost phone: deactivate the device/session, revoke all user sessions, rotate the password if compromise is suspected, and review audit/sync history. Offboarding: deactivate the user, revoke sessions/device tokens, recover company equipment and reassign open work. Security incident: contain, preserve logs, rotate affected secrets, restore known-good images, notify affected operators, document timeline/root cause and add a regression test.

Production secrets must be generated per environment, stored only in root-readable `.env`/Docker secrets and GitHub environment secrets, and rotated at least every 90 days or immediately after exposure. Never expose ports 1433, 9000, 9001, 3000 or 9090 publicly.

The August 2026 mobile audit reports a moderate `uuid` advisory only through Expo CLI's `xcode` build-tool dependency. It is not used by the running application; npm's offered forced remediation downgrades Expo from SDK 57 to 46 and is rejected. Dependabot tracks the upstream fix. Reassess before every mobile release.
