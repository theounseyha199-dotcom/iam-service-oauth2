package kh.edu.iam.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "role_permissions")
@Getter
@Setter
@NoArgsConstructor
public class RolePermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_id", nullable = false, updatable = false)
    private Long roleId;

    @Column(name = "permission_id", nullable = false, updatable = false)
    private Long permissionId;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    @Column(
            name = "assigned_by",
            nullable = false,
            updatable = false,
            length = 100
    )
    private String assignedBy;

    @Column(name = "expires_at", updatable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoked_by", length = 100)
    private String revokedBy;

    @Column(length = 255, updatable = false)
    private String reason;

    @Column(name = "revocation_reason", length = 255)
    private String revocationReason;
}