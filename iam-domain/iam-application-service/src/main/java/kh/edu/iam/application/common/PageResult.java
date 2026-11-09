package kh.edu.iam.application.common;

import java.util.List;

public record PageResult<T>(List<T> content, Metadata page) {
    public record Metadata(int number, int size, long totalElements, int totalPages) {}

    public PageResult {
        content = List.copyOf(content);
    }

    public PageResult(List<T> content, int number, int size, long totalElements, int totalPages) {
        this(content, new Metadata(number, size, totalElements, totalPages));
    }

    public int number() {
        return page.number();
    }

    public int size() {
        return page.size();
    }

    public long totalElements() {
        return page.totalElements();
    }

    public int totalPages() {
        return page.totalPages();
    }
}
