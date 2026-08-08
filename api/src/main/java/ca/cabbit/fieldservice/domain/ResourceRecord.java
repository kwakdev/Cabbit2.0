package ca.cabbit.fieldservice.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="resource_records", indexes={
        @Index(name="ix_resource_company_type", columnList="company_id,resource_type"),
        @Index(name="ix_resource_updated", columnList="company_id,updated_at")})
public class ResourceRecord {
    @Id public UUID id;
    @Column(name="company_id", nullable=false) public UUID companyId;
    @Column(name="resource_type", nullable=false, length=40) public String resourceType;
    @Lob @Column(nullable=false) public String data;
    @Column(name="created_by", nullable=false) public UUID createdBy;
    @Column(name="updated_by", nullable=false) public UUID updatedBy;
    @Column(name="created_at", nullable=false) public Instant createdAt;
    @Column(name="updated_at", nullable=false) public Instant updatedAt;
    @Column(name="deleted_at") public Instant deletedAt;
    @Version @Column(nullable=false) public long version;
    protected ResourceRecord() {}
    public ResourceRecord(UUID id, UUID companyId, String type, String data, UUID actor) {
        this.id=id; this.companyId=companyId; this.resourceType=type; this.data=data;
        this.createdBy=actor; this.updatedBy=actor; this.createdAt=Instant.now(); this.updatedAt=this.createdAt;
    }
}

