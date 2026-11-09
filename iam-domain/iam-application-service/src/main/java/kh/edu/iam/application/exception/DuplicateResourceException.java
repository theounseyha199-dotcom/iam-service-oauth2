package kh.edu.iam.application.exception;

public final class DuplicateResourceException extends ApplicationException {
    public DuplicateResourceException(String message) {
        super("conflict", message);
    }
}
