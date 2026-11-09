package kh.edu.iam.application.permission.usecase;

import kh.edu.iam.application.exception.ResourceNotFoundException;
import kh.edu.iam.application.permission.command.UpdatePermissionCommand;
import kh.edu.iam.application.permission.result.PermissionResult;
import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.PermissionRepository;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Objects;

@RequiredArgsConstructor
public final class UpdatePermissionUseCase {

    private final PermissionRepository repository;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public PermissionResult execute(UpdatePermissionCommand command) {
        Objects.requireNonNull(command, "command is required");

        var permission = repository.findByUuid(command.permissionUuid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Permission not found: " + command.permissionUuid()
                ));

        permission.updateDescription(
                command.description(),
                actorProvider.currentActor(),
                clock.instant()
        );

        return PermissionResult.from(repository.save(permission));
    }
}