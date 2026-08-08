package ca.cabbit.fieldservice.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="audit_events")
public class AuditEvent {
    @Id public UUID id = UUID.randomUUID();
    @Column(name="company_id", nullable=false) public UUID companyId;
    @Column(name="user_id", nullable=false) public UUID userId;
    @Column(nullable=false) public String action;
    @Column(name="entity_type", nullable=false) public String entityType;
    @Column(name="entity_id") public UUID entityId;
    @Lob public String details;
    @Column(name="occurred_at", nullable=false) public Instant occurredAt = Instant.now();
    protected AuditEvent() {}
    public AuditEvent(UUID companyId, UUID userId, String action, String entityType, UUID entityId, String details) {
        this.companyId=companyId; this.userId=userId; this.action=action; this.entityType=entityType; this.entityId=entityId; this.details=details;
    }
}

