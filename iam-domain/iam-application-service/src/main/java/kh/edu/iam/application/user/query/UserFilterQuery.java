package kh.edu.iam.application.user.query;

import java.util.Set;

public record UserFilterQuery(
        String search, Boolean enabled, int page, int size, String sortBy, boolean descending, Boolean locked) {
    public UserFilterQuery {
        search = search == null ? "" : search.strip();
        sortBy = sortBy == null ? "username" : sortBy;
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Invalid page or size");
        }
        if (!Set.of("username", "email", "createdAt", "updatedAt").contains(sortBy)) {
            throw new IllegalArgumentException("Unsupported sort field");
        }
    }

    public UserFilterQuery(String search, Boolean enabled, int page, int size, String sortBy, boolean descending) {
        this(search, enabled, page, size, sortBy, descending, null);
    }

    public UserFilterQuery(int page, int size, String sort, String search, Boolean enabled, Boolean locked) {
        this(
                search,
                enabled,
                page,
                size,
                kh.edu.iam.application.common.PageSort.field(sort),
                kh.edu.iam.application.common.PageSort.descending(sort),
                locked);
    }
}
