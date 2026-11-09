package kh.edu.iam.persistence.repository;

import jakarta.persistence.LockModeType;
import kh.edu.iam.persistence.entity.PermissionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PermissionJpaRepository extends JpaRepository<PermissionEntity , Long> {
    Optional<PermissionEntity> findByUuidAndDeletedFalse(UUID uuid);

    Optional<PermissionEntity> findByName(String name);

    boolean existsByName(String name);

    @Query("""
        select p
        from PermissionEntity p
        where p.deleted = false
          and (:active is null or p.active = :active)
          and locate(lower(:search), lower(p.name)) > 0
        """)
    Page<PermissionEntity> search(
            @Param("search") String search,
            @Param("active") Boolean active,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select permission
        from PermissionEntity permission
        where permission.uuid = :uuid
          and permission.deleted = false
        """)
    Optional<PermissionEntity> findForUpdate(
            @Param("uuid") UUID uuid
    );

}
