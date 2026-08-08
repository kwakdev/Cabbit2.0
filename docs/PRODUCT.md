# Product definition

## Initial market

The first release targets small field-service contractors, with neutral terms that also fit landscaping, pools, nurseries and detailing. Common jobs include inspections, recurring maintenance, installation, cleanup, repair and emergency call-outs.

## Personas

- Administrator: company settings, users, roles, retention and audit access.
- Manager: customers, scheduling, assignment, review, approvals, reporting and incidents.
- Crew leader: assigned crew coordination and field completion.
- Worker: assigned jobs, checklist, time, materials, equipment, photos and incident entry.
- Office worker: customer/location maintenance and read-only operational reporting.

## Core workflow

Customer request -> customer/location lookup -> work order -> schedule/assign -> travel/work timer -> checklist/photos/materials/equipment -> incidents if needed -> completion validation -> manager review -> weekly reporting.

## Business rules

- Administrators and managers create, assign, cancel, reopen and review work orders.
- Crew leaders and workers update only work orders assigned to them.
- Completion requires every required checklist item and all configured photo types.
- Completed work is immutable to field roles. A manager may reopen it, with an audited reason.
- Managers approve/reject time. Overlapping entries are rejected and offline overlap is flagged for manual review.
- Critical damage/safety reports generate manager notifications.
- Every tenant-owned record carries `company_id`; authorization always derives the tenant from the authenticated token, never request input.

## MVP and exclusions

MVP includes authentication/RBAC, companies/users, customers/locations, work orders/assignments, checklists, photos, materials/equipment, time, incidents, offline sync, notifications, dashboards, search and CSV/PDF exports. Invoicing, payroll execution, payments, customer portal, GPS/geofencing, messaging, QuickBooks and route optimization are post-MVP.

## Success measures

- >=95% completed jobs contain required photos and checklist data.
- >=99% queued offline operations eventually synchronize without data loss.
- Weekly reporting takes under five minutes.
- Manager follow-up for missing field data falls by at least 50% during pilot.

