package kh.edu.iam.application.exception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException (String message) {
        super(message);

    }
}

/**
 Later, your REST exception handler can translate this
 into HTTP 409 Conflict. Keep HTTP handling outside
 the application module.
 */