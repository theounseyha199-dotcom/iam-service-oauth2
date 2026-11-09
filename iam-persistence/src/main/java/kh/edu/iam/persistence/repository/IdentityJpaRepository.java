package kh.edu.iam.persistence.repository;

import kh.edu.iam.IdentityProvider;
import kh.edu.iam.persistence.entity.IdentityEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdentityJpaRepository extends JpaRepository<IdentityEntity, Long> {
    Optional<IdentityEntity> findByProviderAndProviderSubject(IdentityProvider provider, String subject);

    Optional<IdentityEntity> findByUuidAndDeletedFalse(UUID uuid);

    List<IdentityEntity> findByUserIdAndDeletedFalseOrderById(Long userId);
}
