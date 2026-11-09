package kh.edu.iam.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import kh.edu.iam.PermissionEffect;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_permissions")
@Getter
@Setter
@NoArgsConstructor
public class UserPermissionEntity extends UserAssignmentEntity {
    @Column(name = "permission_id", nullable = false, updatable = false)
    private Long permissionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5, updatable = false)
    private PermissionEffect effect;
}
