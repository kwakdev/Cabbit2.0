CREATE TABLE companies (
  id uniqueidentifier NOT NULL PRIMARY KEY,
  name nvarchar(200) NOT NULL,
  timezone nvarchar(80) NOT NULL DEFAULT 'America/Toronto',
  settings nvarchar(max) NULL,
  created_at datetime2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at datetime2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at datetime2 NULL,
  version bigint NOT NULL DEFAULT 0
);

CREATE TABLE app_users (
  id uniqueidentifier NOT NULL PRIMARY KEY,
  company_id uniqueidentifier NOT NULL,
  email nvarchar(320) NOT NULL,
  password_hash nvarchar(100) NOT NULL,
  display_name nvarchar(200) NOT NULL,
  role nvarchar(40) NOT NULL,
  active bit NOT NULL DEFAULT 1,
  must_change_password bit NOT NULL DEFAULT 1,
  failed_logins int NOT NULL DEFAULT 0,
  locked_until datetime2 NULL,
  created_at datetime2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
  version bigint NOT NULL DEFAULT 0,
  CONSTRAINT uq_users_email UNIQUE(email)
);

CREATE TABLE refresh_tokens (
  id uniqueidentifier NOT NULL PRIMARY KEY,
  user_id uniqueidentifier NOT NULL,
  token_hash char(64) NOT NULL,
  device_id nvarchar(200) NOT NULL,
  expires_at datetime2 NOT NULL,
  revoked_at datetime2 NULL,
  created_at datetime2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_refresh_hash UNIQUE(token_hash),
  CONSTRAINT fk_refresh_user FOREIGN KEY(user_id) REFERENCES app_users(id)
);

CREATE TABLE resource_records (
  id uniqueidentifier NOT NULL PRIMARY KEY,
  company_id uniqueidentifier NOT NULL,
  resource_type nvarchar(40) NOT NULL,
  data nvarchar(max) NOT NULL,
  created_by uniqueidentifier NOT NULL,
  updated_by uniqueidentifier NOT NULL,
  created_at datetime2 NOT NULL,
  updated_at datetime2 NOT NULL,
  deleted_at datetime2 NULL,
  version bigint NOT NULL DEFAULT 0
);
CREATE INDEX ix_resource_company_type ON resource_records(company_id, resource_type);
CREATE INDEX ix_resource_updated ON resource_records(company_id, updated_at);

CREATE TABLE sync_operations (
  operation_id uniqueidentifier NOT NULL PRIMARY KEY,
  company_id uniqueidentifier NOT NULL,
  user_id uniqueidentifier NOT NULL,
  device_id nvarchar(200) NOT NULL,
  entity_type nvarchar(40) NOT NULL,
  entity_id uniqueidentifier NOT NULL,
  action nvarchar(20) NOT NULL,
  status nvarchar(20) NOT NULL,
  details nvarchar(max) NULL,
  processed_at datetime2 NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX ix_sync_company_status ON sync_operations(company_id, status, processed_at);

CREATE TABLE audit_events (
  id uniqueidentifier NOT NULL PRIMARY KEY,
  company_id uniqueidentifier NOT NULL,
  user_id uniqueidentifier NOT NULL,
  action nvarchar(60) NOT NULL,
  entity_type nvarchar(40) NOT NULL,
  entity_id uniqueidentifier NULL,
  details nvarchar(max) NULL,
  occurred_at datetime2 NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX ix_audit_company_time ON audit_events(company_id, occurred_at);

-- Normalized operational projections. The API's versioned resource envelope is the
-- offline write model; these tables are available for reporting projections and future
-- domain-specific modules without a destructive migration.
CREATE TABLE customers (id uniqueidentifier PRIMARY KEY, company_id uniqueidentifier NOT NULL, name nvarchar(200) NOT NULL, email nvarchar(320), phone nvarchar(60), archived_at datetime2, created_at datetime2 NOT NULL, updated_at datetime2 NOT NULL, version bigint NOT NULL DEFAULT 0);
CREATE INDEX ix_customers_company_name ON customers(company_id,name);
CREATE TABLE service_locations (id uniqueidentifier PRIMARY KEY, company_id uniqueidentifier NOT NULL, customer_id uniqueidentifier NOT NULL, name nvarchar(200) NOT NULL, address nvarchar(500), contact_name nvarchar(200), contact_phone nvarchar(60), access_instructions nvarchar(max), gate_code nvarchar(200), latitude decimal(9,6), longitude decimal(9,6), archived_at datetime2, created_at datetime2 NOT NULL, updated_at datetime2 NOT NULL, version bigint NOT NULL DEFAULT 0, CONSTRAINT fk_location_customer FOREIGN KEY(customer_id) REFERENCES customers(id));
CREATE TABLE work_orders (id uniqueidentifier PRIMARY KEY, company_id uniqueidentifier NOT NULL, work_order_number nvarchar(40) NOT NULL, customer_id uniqueidentifier NOT NULL, location_id uniqueidentifier NOT NULL, title nvarchar(250) NOT NULL, description nvarchar(max), priority nvarchar(20) NOT NULL, status nvarchar(30) NOT NULL, scheduled_start datetime2, scheduled_end datetime2, estimated_minutes int, instructions nvarchar(max), internal_notes nvarchar(max), completion_notes nvarchar(max), cancellation_reason nvarchar(500), created_at datetime2 NOT NULL, updated_at datetime2 NOT NULL, deleted_at datetime2, version bigint NOT NULL DEFAULT 0, CONSTRAINT uq_work_number UNIQUE(company_id,work_order_number));
CREATE INDEX ix_work_company_schedule ON work_orders(company_id,scheduled_start,status);
CREATE TABLE work_order_assignments (id uniqueidentifier PRIMARY KEY, company_id uniqueidentifier NOT NULL, work_order_id uniqueidentifier NOT NULL, user_id uniqueidentifier NOT NULL, crew_leader bit NOT NULL DEFAULT 0, assigned_at datetime2 NOT NULL, removed_at datetime2, version bigint NOT NULL DEFAULT 0);
CREATE INDEX ix_assignment_user ON work_order_assignments(company_id,user_id,assigned_at);
CREATE TABLE work_order_status_history (id uniqueidentifier PRIMARY KEY, company_id uniqueidentifier NOT NULL, work_order_id uniqueidentifier NOT NULL, old_status nvarchar(30), new_status nvarchar(30) NOT NULL, reason nvarchar(500), changed_by uniqueidentifier NOT NULL, changed_at datetime2 NOT NULL);
CREATE TABLE checklist_templates (id uniqueidentifier PRIMARY KEY, company_id uniqueidentifier NOT NULL, name nvarchar(200) NOT NULL, archived_at datetime2, created_at datetime2 NOT NULL, updated_at datetime2 NOT NULL, version bigint NOT NULL DEFAULT 0);
CREATE TABLE checklist_template_items (id uniqueidentifier PRIMARY KEY, company_id uniqueidentifier NOT NULL, template_id uniqueidentifier NOT NULL, item_order int NOT NULL, label nvarchar(500) NOT NULL, item_type nvarchar(30) NOT NULL, required bit NOT NULL DEFAULT 0, instructions nvarchar(max), options_json nvarchar(max), condition_json nvarchar(max), version bigint NOT NULL DEFAULT 0);
CREATE TABLE work_order_checklists (id uniqueidentifier PRIMARY KEY, company_id uniqueidentifier NOT NULL, work_order_id uniqueidentifier NOT NULL, template_id uniqueidentifier NOT NULL, status nvarchar(30) NOT NULL, progress_percent int NOT NULL DEFAULT 0, version bigint NOT NULL DEFAULT 0);
CREATE TABLE checklist_responses (id uniqueidentifier PRIMARY KEY, company_id uniqueidentifier NOT NULL, checklist_id uniqueidentifier NOT NULL, item_id uniqueidentifier NOT NULL, response_json nvarchar(max), passed bit, answered_by uniqueidentifier, answered_at datetime2, version bigint NOT NULL DEFAULT 0);
CREATE TABLE photo_metadata (id uniqueidentifier PRIMARY KEY, company_id uniqueidentifier NOT NULL, work_order_id uniqueidentifier, incident_id uniqueidentifier, object_key nvarchar(500) NOT NULL, thumbnail_key nvarchar(500), photo_type nvarchar(30) NOT NULL, mime_type nvarchar(100) NOT NULL, size_bytes bigint NOT NULL, caption nvarchar(500), captured_at datetime2, uploaded_by uniqueidentifier NOT NULL, sha256 char(64), created_at datetime2 NOT NULL, deleted_at datetime2, CONSTRAINT uq_photo_hash UNIQUE(company_id,sha256));
CREATE TABLE materials (id uniqueidentifier PRIMARY KEY, company_id uniqueidentifier NOT NULL, name nvarchar(200) NOT NULL, sku nvarchar(100), unit nvarchar(30) NOT NULL, default_cost decimal(12,2), archived_at datetime2, version bigint NOT NULL DEFAULT 0);
CREATE TABLE work_order_materials (id uniqueidentifier PRIMARY KEY, company_id uniqueidentifier NOT NULL, work_order_id uniqueidentifier NOT NULL, material_id uniqueidentifier, custom_name nvarchar(200), quantity decimal(12,3) NOT NULL, waste_quantity decimal(12,3) NOT NULL DEFAULT 0, unit_cost decimal(12,2), notes nvarchar(500), recorded_by uniqueidentifier NOT NULL, recorded_at datetime2 NOT NULL, version bigint NOT NULL DEFAULT 0);
CREATE TABLE equipment (id uniqueidentifier PRIMARY KEY, company_id uniqueidentifier NOT NULL, name nvarchar(200) NOT NULL, category nvarchar(100), serial_number nvarchar(100), maintenance_status nvarchar(40), archived_at datetime2, version bigint NOT NULL DEFAULT 0);
CREATE TABLE work_order_equipment (id uniqueidentifier PRIMARY KEY, company_id uniqueidentifier NOT NULL, work_order_id uniqueidentifier NOT NULL, equipment_id uniqueidentifier NOT NULL, duration_minutes int, damaged bit NOT NULL DEFAULT 0, notes nvarchar(500), used_by uniqueidentifier, used_at datetime2, version bigint NOT NULL DEFAULT 0);
CREATE TABLE time_entries (id uniqueidentifier PRIMARY KEY, company_id uniqueidentifier NOT NULL, work_order_id uniqueidentifier NOT NULL, user_id uniqueidentifier NOT NULL, category nvarchar(30) NOT NULL, started_at datetime2 NOT NULL, ended_at datetime2, device_started_at datetime2, approval_status nvarchar(30) NOT NULL DEFAULT 'PENDING', rejection_reason nvarchar(500), approved_by uniqueidentifier, approved_at datetime2, hourly_cost decimal(12,2), version bigint NOT NULL DEFAULT 0);
CREATE INDEX ix_time_user_start ON time_entries(company_id,user_id,started_at);
CREATE TABLE break_entries (id uniqueidentifier PRIMARY KEY, company_id uniqueidentifier NOT NULL, time_entry_id uniqueidentifier NOT NULL, started_at datetime2 NOT NULL, ended_at datetime2, version bigint NOT NULL DEFAULT 0);
CREATE TABLE incidents (id uniqueidentifier PRIMARY KEY, company_id uniqueidentifier NOT NULL, work_order_id uniqueidentifier, incident_type nvarchar(30) NOT NULL, category nvarchar(60) NOT NULL, severity nvarchar(30) NOT NULL, description nvarchar(max) NOT NULL, immediate_action nvarchar(max), status nvarchar(30) NOT NULL DEFAULT 'OPEN', acknowledged_by uniqueidentifier, acknowledged_at datetime2, resolution_notes nvarchar(max), reported_by uniqueidentifier NOT NULL, reported_at datetime2 NOT NULL, version bigint NOT NULL DEFAULT 0);
CREATE TABLE notifications (id uniqueidentifier PRIMARY KEY, company_id uniqueidentifier NOT NULL, user_id uniqueidentifier NOT NULL, notification_type nvarchar(50) NOT NULL, title nvarchar(200) NOT NULL, body nvarchar(1000) NOT NULL, data_json nvarchar(max), read_at datetime2, created_at datetime2 NOT NULL);
CREATE INDEX ix_notifications_user ON notifications(company_id,user_id,read_at,created_at);
CREATE TABLE devices (id uniqueidentifier PRIMARY KEY, company_id uniqueidentifier NOT NULL, user_id uniqueidentifier NOT NULL, device_identifier nvarchar(200) NOT NULL, push_token nvarchar(500), platform nvarchar(20), last_seen_at datetime2, revoked_at datetime2, CONSTRAINT uq_device UNIQUE(user_id,device_identifier));
CREATE TABLE recurring_work_orders (id uniqueidentifier PRIMARY KEY, company_id uniqueidentifier NOT NULL, template_work_order_id uniqueidentifier NOT NULL, recurrence_rule nvarchar(500) NOT NULL, next_occurrence datetime2, active bit NOT NULL DEFAULT 1, version bigint NOT NULL DEFAULT 0);

