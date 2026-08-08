package ca.cabbit.fieldservice.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="refresh_tokens")
public class RefreshToken {
    @Id public UUID id;
    @Column(name="user_id", nullable=false) public UUID userId;
    @Column(name="token_hash", nullable=false, unique=true, length=64) public String tokenHash;
    @Column(name="device_id", nullable=false) public String deviceId;
    @Column(name="expires_at", nullable=false) public Instant expiresAt;
    @Column(name="revoked_at") public Instant revokedAt;
    @Column(name="created_at", nullable=false) public Instant createdAt = Instant.now();
    protected RefreshToken() {}
    public RefreshToken(UUID id, UUID userId, String tokenHash, String deviceId, Instant expiresAt) {
        this.id=id; this.userId=userId; this.tokenHash=tokenHash; this.deviceId=deviceId; this.expiresAt=expiresAt;
    }
}

