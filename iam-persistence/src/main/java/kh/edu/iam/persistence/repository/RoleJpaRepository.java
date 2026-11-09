package kh.edu.iam.persistence.repository;

import kh.edu.iam.persistence.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleJpaRepository
        extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByUuidAndDeletedFalse(UUID uuid);

    boolean existsByName(String name);
}
