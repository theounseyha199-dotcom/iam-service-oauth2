package kh.edu.iam.application.user.usecase;

import kh.edu.iam.application.common.PageResult;
import kh.edu.iam.application.port.out.TransactionRunner;
import kh.edu.iam.application.port.out.UserRepository;
import kh.edu.iam.application.user.query.UserFilterQuery;
import kh.edu.iam.application.user.result.UserSummary;

import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
public final class ListUsersUseCase {
    private final UserRepository users;
    private final TransactionRunner transactions;

    public PageResult<UserSummary> execute(UserFilterQuery query) {
        return transactions.required(
                () -> {
                    var page = users.findAll(Objects.requireNonNull(query));
                    return new PageResult<>(
                            page.content().stream().map(UserSummary::from).toList(),
                            page.number(),
                            page.size(),
                            page.totalElements(),
                            page.totalPages());
                });
    }
}
