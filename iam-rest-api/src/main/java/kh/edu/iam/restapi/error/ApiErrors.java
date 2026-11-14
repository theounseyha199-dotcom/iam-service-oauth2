package kh.edu.iam.restapi.error;

import kh.edu.iam.application.exception.ApplicationException;
import kh.edu.iam.domain.exception.DomainException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@RestControllerAdvice(basePackages = {"kh.edu.iam.restapi.controller", "kh.edu.iam.infrastructure.workspace"})
public class ApiErrors extends ResponseEntityExceptionHandler {
    public record FieldError(String field, String message) {}

    public static ProblemDetail problem(HttpStatus status, String code, String detail) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("https://iam.istad.co/problems/" + code.replace('_', '-')));
        problem.setTitle(status == HttpStatus.BAD_REQUEST ? "Validation failed" : status.getReasonPhrase());
        return problem;
    }

    @ExceptionHandler(ApplicationException.class)
    ProblemDetail application(ApplicationException ex) {
        var result = failure(ex.code(), ex.getMessage());
        if (!ex.errors().isEmpty()) {
            result.setProperty(
                    "errors",
                    ex.errors().entrySet().stream().map(e -> new FieldError(e.getKey(), e.getValue())).toList());
        }
        return result;
    }

    @ExceptionHandler(DomainException.class)
    ProblemDetail domain(DomainException ex) {
        return failure(ex.code(), ex.getMessage());
    }

    private ProblemDetail failure(String code, String detail) {
        HttpStatus status =
                switch (code) {
                    case "not_found", "identity_not_found" -> HttpStatus.NOT_FOUND;
                    case "validation", "invalid_current_password", "profile_completion_required" ->
                            HttpStatus.BAD_REQUEST;
                    default -> HttpStatus.CONFLICT;
                };
        var result = problem(status, status == HttpStatus.BAD_REQUEST ? "validation-error" : code, detail);
        result.setProperty("code", code);
        if (status == HttpStatus.BAD_REQUEST) {
            result.setProperty("errors", List.of(new FieldError("request", detail)));
        }
        return result;
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail forbidden(AccessDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN, "forbidden", "You do not have permission to perform this operation");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var result = problem(HttpStatus.BAD_REQUEST, "validation-error", "Correct the invalid request fields");
        result.setProperty(
                "errors",
                ex.getBindingResult().getFieldErrors().stream()
                        .map(
                                e ->
                                        new FieldError(
                                                e.getField(),
                                                Objects.requireNonNullElse(e.getDefaultMessage(), "Invalid value")))
                        .toList());
        return handleExceptionInternal(ex, result, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // Never return parser messages: they may contain passwords or provider proof.
        var result =
                problem(
                        HttpStatus.BAD_REQUEST,
                        "validation-error",
                        "Malformed JSON, unsupported field, or invalid value type");
        result.setProperty("errors", List.of(new FieldError("request", "Check the JSON fields and value types")));
        return handleExceptionInternal(ex, result, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            org.springframework.beans.TypeMismatchException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        String field = ex instanceof MethodArgumentTypeMismatchException mismatch ? mismatch.getName() : "request";
        var result = problem(HttpStatus.BAD_REQUEST, "validation-error", "Invalid path or query parameter");
        result.setProperty("errors", List.of(new FieldError(field, "Invalid value type")));
        return handleExceptionInternal(ex, result, headers, status, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail invalid(IllegalArgumentException ex) {
        return failure("validation", "Correct the invalid request values");
    }

    @ExceptionHandler(IllegalStateException.class)
    ProblemDetail invalidState(IllegalStateException ex) {
        return failure("state_conflict", "The current record state does not allow this operation");
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    ProblemDetail duplicate(org.springframework.dao.DataIntegrityViolationException ex) {
        return failure("conflict", "A unique value is already in use or a related record is unavailable");
    }

    @ExceptionHandler(org.springframework.dao.OptimisticLockingFailureException.class)
    ProblemDetail concurrent(org.springframework.dao.OptimisticLockingFailureException ex) {
        return failure("concurrent_update", "The record changed; reload it before saving again");
    }
}
