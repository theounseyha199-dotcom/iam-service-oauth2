package kh.edu.iam.application.port.out;

import kh.edu.iam.Identity;
import kh.edu.iam.IdentityProvider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdentityRepository {
    void lockProviderSubject(IdentityProvider provider, String subject);

    Optional<Identity> findByProviderSubject(IdentityProvider provider, String subject);

    Optional<Identity> findByUuid(UUID uuid);

    List<Identity> findByUserId(Long userId);

    Identity save(Identity identity);
}
