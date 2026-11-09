package kh.edu.iam.application.permission.usecase;

import kh.edu.iam.application.exception.ResourceNotFoundException;
import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.PermissionRepository;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public final class DeletePermissionUseCase {

    private final PermissionRepository repository;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public void execute(UUID permissionUuid) {
        Objects.requireNonNull(permissionUuid, "permissionUuid is required");

        var permission = repository.findByUuid(permissionUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Permission not found: " + permissionUuid
                ));

        permission.delete(
                actorProvider.currentActor(),
                clock.instant()
        );

        repository.save(permission);
    }
}