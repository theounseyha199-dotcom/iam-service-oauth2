package kh.edu.iam.application.permission.usecase;

import kh.edu.iam.Permission;
import kh.edu.iam.application.exception.DuplicateResourceException;
import kh.edu.iam.application.permission.command.CreatePermissionCommand;
import kh.edu.iam.application.permission.result.PermissionResult;
import kh.edu.iam.application.port.out.CurrentActorProvider;
import kh.edu.iam.application.port.out.PermissionRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Clock;
import java.util.Objects;


@Getter
@Setter
public class CreatePermissionUseCase {

    private final PermissionRepository permissionRepository;
    private final CurrentActorProvider currentActorProvider;
    private final Clock clock;


    public CreatePermissionUseCase(PermissionRepository permissionRepository,
                                   CurrentActorProvider currentActorProvider,
                                   Clock clock) {

        this.permissionRepository = permissionRepository;
        this.currentActorProvider = currentActorProvider;
        this.clock = clock;
    }

    public PermissionResult execute(CreatePermissionCommand command) {
        Objects.requireNonNull(command, "command is required");

        // The domain factory validates the supplied fields.
        Permission permission = Permission.create(
                command.resource(),
                command.action(),
                command.description(),
                currentActorProvider.currentActor(),
                clock.instant()
        );

        if (permissionRepository.existsByName(permission.getName())) {
            throw new DuplicateResourceException(
                    "Permission already exists: " + permission.getName()
            );
        }

        Permission saved = permissionRepository.save(permission);

        return PermissionResult.from(saved);
    }
}


