package kh.edu.iam.application.role.usecase;

import kh.edu.iam.application.exception.ResourceNotFoundException;
import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.RoleRepository;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public final class DeleteRoleUseCase {

    private final RoleRepository repository;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public void execute(UUID roleUuid) {
        Objects.requireNonNull(roleUuid, "roleUuid is required");

        var role = repository.findByUuid(roleUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found: " + roleUuid
                ));

        role.delete(actorProvider.currentActor(), clock.instant());

        repository.save(role);
    }
}