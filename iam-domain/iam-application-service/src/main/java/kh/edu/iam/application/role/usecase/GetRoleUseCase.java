package kh.edu.iam.application.role.usecase;

import kh.edu.iam.application.exception.ResourceNotFoundException;
import kh.edu.iam.application.port.out.RoleRepository;
import kh.edu.iam.application.role.result.RoleResult;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public final class GetRoleUseCase {

    private final RoleRepository repository;

    public RoleResult execute(UUID roleUuid) {
        Objects.requireNonNull(roleUuid, "roleUuid is required");

        return repository.findByUuid(roleUuid)
                .map(RoleResult::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found: " + roleUuid
                ));
    }
}
