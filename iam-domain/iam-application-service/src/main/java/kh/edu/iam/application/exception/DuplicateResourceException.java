package kh.edu.iam.application.exception;

public final class DuplicateResourceException extends ApplicationException {
    public DuplicateResourceException(String message, String field) {
        super("conflict", message, java.util.Map.of(field, "duplicate"));
    }

    public DuplicateResourceException(String message) {
        super("conflict", message);
    }
}
