package kh.edu.iam.application.common;

import java.util.List;

public record PageResult<T>(
        List<T> content,
        int number,
        int size,
        long totalElements,
        int totalPages
) {
    public PageResult {
        content = List.copyOf(content);
    }
}