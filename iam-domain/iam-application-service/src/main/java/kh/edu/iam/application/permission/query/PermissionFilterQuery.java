package kh.edu.iam.application.permission.query;

import kh.edu.iam.application.common.PageSort;

import java.util.Set;

public record PermissionFilterQuery(
        String search,
        Boolean active,
        int page,
        int size,
        String sortBy,
        boolean descending,
        String resource,
        String action) {
    public PermissionFilterQuery {
        search = search == null ? "" : search.strip();
        sortBy = sortBy == null ? "name" : sortBy;
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Invalid page or size");
        }
        if (!Set.of("name", "resource", "action", "createdAt", "updatedAt").contains(sortBy)) {
            throw new IllegalArgumentException("Unsupported sort field");
        }
    }

    public PermissionFilterQuery(String search, Boolean active, int page, int size, String sortBy, boolean descending) {
        this(search, active, page, size, sortBy, descending, null, null);
    }

    public PermissionFilterQuery(
            int page, int size, String sort, String search, String resource, String action, Boolean active) {
        this(search, active, page, size, PageSort.field(sort), PageSort.descending(sort), resource, action);
    }
}
