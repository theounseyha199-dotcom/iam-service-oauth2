package kh.edu.iam.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import kh.edu.iam.IdentityProvider;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "identities")
@Getter
@Setter
@NoArgsConstructor
public class IdentityEntity extends AuditedEntity {
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private IdentityProvider provider;

    @Column(name = "provider_subject", nullable = false, length = 255)
    private String providerSubject;

    @Column(name = "email", nullable = true, length = 255)
    private String email;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "display_name", nullable = true, length = 150)
    private String displayName;

    @Column(name = "username", nullable = true, length = 100)
    private String username;

    @Column(name = "picture_url", nullable = true, length = 500)
    private String pictureUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes", nullable = true, columnDefinition = "jsonb")
    private String attributes;

    @Column(name = "linked_at", nullable = false)
    private Instant linkedAt;

    @Column(name = "last_login_at", nullable = true)
    private Instant lastLoginAt;
}
