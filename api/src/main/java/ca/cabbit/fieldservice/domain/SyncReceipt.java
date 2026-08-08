package ca.cabbit.fieldservice.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="sync_operations")
public class SyncReceipt {
    @Id @Column(name="operation_id") public UUID operationId;
    @Column(name="company_id", nullable=false) public UUID companyId;
    @Column(name="user_id", nullable=false) public UUID userId;
    @Column(name="device_id", nullable=false) public String deviceId;
    @Column(name="entity_type", nullable=false) public String entityType;
    @Column(name="entity_id", nullable=false) public UUID entityId;
    @Column(nullable=false) public String action;
    @Column(nullable=false) public String status;
    @Lob public String details;
    @Column(name="processed_at", nullable=false) public Instant processedAt = Instant.now();
    protected SyncReceipt() {}
    public SyncReceipt(UUID operationId, UUID companyId, UUID userId, String deviceId, String entityType, UUID entityId, String action, String status, String details) {
        this.operationId=operationId; this.companyId=companyId; this.userId=userId; this.deviceId=deviceId;
        this.entityType=entityType; this.entityId=entityId; this.action=action; this.status=status; this.details=details;
    }
}

