package kh.edu.iam.persistence.repository;

import kh.edu.iam.persistence.entity.RoleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RoleJpaRepository
        extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByUuidAndDeletedFalse(UUID uuid);

    boolean existsByName(String name);

    @Query("""
        select r
        from RoleEntity r
        where r.deleted = false
          and (:active is null or r.active = :active)
          and locate(lower(:search), lower(r.name)) > 0
        """)
    Page<RoleEntity> search(
            @Param("search") String search,
            @Param("active") Boolean active,
            Pageable pageable
    );

}
