package kh.edu.iam.persistence.repository;

import jakarta.persistence.LockModeType;

import kh.edu.iam.persistence.entity.UserRoleEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRoleJpaRepository extends JpaRepository<UserRoleEntity, Long> {
    List<UserRoleEntity> findByUserIdOrderById(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from UserRoleEntity a where a.id = :id")
    Optional<UserRoleEntity> findForUpdate(@Param("id") Long id);
}
