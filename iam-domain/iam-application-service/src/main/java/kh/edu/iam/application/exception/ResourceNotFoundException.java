package kh.edu.iam.application.exception;

public final class ResourceNotFoundException extends ApplicationException {
    public ResourceNotFoundException(String message) {
        super("not_found", message);
    }
}
