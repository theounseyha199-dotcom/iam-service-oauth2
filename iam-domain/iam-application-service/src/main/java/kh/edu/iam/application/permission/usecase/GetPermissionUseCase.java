package kh.edu.iam.application.permission.usecase;

import kh.edu.iam.application.exception.ResourceNotFoundException;
import kh.edu.iam.application.permission.result.PermissionResult;
import kh.edu.iam.application.port.out.PermissionRepository;
import kh.edu.iam.application.port.out.TransactionRunner;

import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public final class GetPermissionUseCase {

    private final TransactionRunner transactions;

    private final PermissionRepository repository;

    public PermissionResult execute(UUID permissionUuid) {
        return transactions.required(
                () -> {
                    Objects.requireNonNull(permissionUuid, "permissionUuid is required");

                    return repository
                            .findByUuid(permissionUuid)
                            .map(PermissionResult::from)
                            .orElseThrow(
                                    () -> new ResourceNotFoundException("Permission not found: " + permissionUuid));
                });
    }
}
