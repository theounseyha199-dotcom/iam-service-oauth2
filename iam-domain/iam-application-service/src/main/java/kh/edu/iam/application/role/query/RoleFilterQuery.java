package kh.edu.iam.application.role.query;

import java.util.Set;

public record RoleFilterQuery(
        String search,
        Boolean active,
        int page,
        int size,
        String sortBy,
        boolean descending
) {

    private static final Set<String> SORT_FIELDS = Set.of(
            "name",
            "createdAt",
            "updatedAt"
    );

    public RoleFilterQuery {
        search = search == null ? "" : search.strip();
        sortBy = sortBy == null ? "name" : sortBy;

        if (page < 0) {
            throw new IllegalArgumentException(
                    "page must be zero or greater"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "size must be between 1 and 100"
            );
        }

        if (!SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Unsupported sort field: " + sortBy
            );
        }
    }
}
