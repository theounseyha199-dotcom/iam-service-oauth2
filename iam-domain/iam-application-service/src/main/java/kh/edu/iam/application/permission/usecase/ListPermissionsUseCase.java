package kh.edu.iam.application.permission.usecase;

import kh.edu.iam.application.common.PageResult;
import kh.edu.iam.application.permission.query.PermissionFilterQuery;
import kh.edu.iam.application.permission.result.PermissionResult;
import kh.edu.iam.application.port.out.PermissionRepository;
import kh.edu.iam.application.port.out.TransactionRunner;

import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
public final class ListPermissionsUseCase {

    private final TransactionRunner transactions;

    private final PermissionRepository repository;

    public PageResult<PermissionResult> execute(PermissionFilterQuery query) {
        return transactions.required(
                () -> {
                    Objects.requireNonNull(query, "query is required");

                    var page = repository.findAll(query);

                    return new PageResult<>(
                            page.content().stream().map(PermissionResult::from).toList(),
                            page.number(),
                            page.size(),
                            page.totalElements(),
                            page.totalPages());
                });
    }
}
