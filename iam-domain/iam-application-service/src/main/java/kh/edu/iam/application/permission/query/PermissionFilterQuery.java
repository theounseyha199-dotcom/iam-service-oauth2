package kh.edu.iam.application.permission.query;

import java.util.Set;

public record PermissionFilterQuery (
        String search,
        Boolean active,
        int page,
        int size,
        String sortBy,
        boolean descending
){

    private static final Set<String> SORT_FIELSDS
            = Set.of(

            "name", "resource", "action", "createdAt", "updatedAt"
    );

    public PermissionFilterQuery{
        search = search == null ? "" : search.strip();
        sortBy = sortBy == null ? "" : sortBy;


        if (page < 0){
            throw new IllegalArgumentException("page must be zero or greater");
        }
        if (size < 1){
            throw new IllegalArgumentException("size must be greater than 0");
        }

        if (!SORT_FIELSDS.contains(sortBy)){
            throw new IllegalArgumentException("Unsupported sort field");
        }
    }

}

/*
active == null means include both active and inactive permissions.
 Soft-deleted permissions are always excluded.
 */
