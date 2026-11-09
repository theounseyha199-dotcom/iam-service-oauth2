package kh.edu.iam.persistence.repository;

import jakarta.persistence.LockModeType;

import kh.edu.iam.persistence.entity.UserEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUuidAndDeletedFalse(UUID uuid);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("select u from UserEntity u where u.deleted = false and (u.username = :login or u.email = :email)")
    Optional<UserEntity> findByLogin(@Param("login") String login, @Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserEntity u where u.uuid = :uuid and u.deleted = false")
    Optional<UserEntity> findForUpdate(@Param("uuid") UUID uuid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserEntity u where u.deleted = false and (u.username = :login or u.email = :email)")
    Optional<UserEntity> findByLoginForUpdate(@Param("login") String login, @Param("email") String email);

    @Query(
            """
          select u from UserEntity u where u.deleted = false
            and (:enabled is null or u.enabled = :enabled)
        and (:locked is null or u.accountNonLocked <> :locked)
            and (locate(lower(:search), lower(u.username)) > 0
                 or locate(lower(:search), lower(u.email)) > 0)
        """)
    Page<UserEntity> search(
            @Param("search") String search,
            @Param("enabled") Boolean enabled,
            @Param("locked") Boolean locked,
            Pageable pageable);
}
