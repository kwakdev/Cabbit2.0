package ca.cabbit.fieldservice.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_users")
public class AppUser {
    @Id public UUID id;
    @Column(name="company_id", nullable=false) public UUID companyId;
    @Column(nullable=false, unique=true, length=320) public String email;
    @Column(name="password_hash", nullable=false) public String passwordHash;
    @Column(name="display_name", nullable=false) public String displayName;
    @Enumerated(EnumType.STRING) @Column(nullable=false) public Role role;
    @Column(nullable=false) public boolean active = true;
    @Column(name="must_change_password", nullable=false) public boolean mustChangePassword;
    @Column(name="failed_logins", nullable=false) public int failedLogins;
    @Column(name="locked_until") public Instant lockedUntil;
    @Column(name="created_at", nullable=false) public Instant createdAt = Instant.now();
    @Version @Column(nullable=false) public long version;

    protected AppUser() {}
    public AppUser(UUID companyId, String email, String passwordHash, String displayName, Role role, boolean mustChangePassword) {
        this.id = UUID.randomUUID(); this.companyId = companyId; this.email = email.toLowerCase();
        this.passwordHash = passwordHash; this.displayName = displayName; this.role = role;
        this.mustChangePassword = mustChangePassword;
    }
}

