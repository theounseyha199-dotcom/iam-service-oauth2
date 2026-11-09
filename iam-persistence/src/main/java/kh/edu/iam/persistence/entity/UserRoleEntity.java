package kh.edu.iam.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_roles")
@Getter
@Setter
@NoArgsConstructor
public class UserRoleEntity extends UserAssignmentEntity {
    @Column(name = "role_id", nullable = false, updatable = false)
    private Long roleId;
}
