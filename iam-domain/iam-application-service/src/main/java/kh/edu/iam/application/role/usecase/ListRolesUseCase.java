package kh.edu.iam.application.role.usecase;

import kh.edu.iam.application.common.PageResult;
import kh.edu.iam.application.port.out.RoleRepository;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.role.query.RoleFilterQuery;
import kh.edu.iam.application.role.result.RoleSummary;

import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
public final class ListRolesUseCase {

    private final TransactionRunner transactions;

    private final RoleRepository repository;

    public PageResult<RoleSummary> execute(RoleFilterQuery query) {
        return transactions.required(
                () -> {
                    Objects.requireNonNull(query, "query is required");

                    var page = repository.findAll(query);

                    return new PageResult<>(
                            page.content().stream().map(RoleSummary::from).toList(),
                            page.number(),
                            page.size(),
                            page.totalElements(),
                            page.totalPages());
                });
    }
}
