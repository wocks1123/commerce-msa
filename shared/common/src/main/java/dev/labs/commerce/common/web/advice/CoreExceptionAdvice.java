package dev.labs.commerce.common.web.advice;

import dev.labs.commerce.common.error.*;
import dev.labs.commerce.common.web.problem.ProblemDetailFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(1)
@Slf4j
public class CoreExceptionAdvice {

    private final ProblemDetailFactory problemDetailFactory;

    public CoreExceptionAdvice(ProblemDetailFactory problemDetailFactory) {
        this.problemDetailFactory = problemDetailFactory;
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(NotFoundException ex,
                                                        HttpServletRequest request) {
        return build(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflict(ConflictException ex,
                                                        HttpServletRequest request) {
        return build(ex, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ProblemDetail> handleValidation(ValidationException ex,
                                                          HttpServletRequest request) {
        return build(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(DependencyTimeoutException.class)
    public ResponseEntity<ProblemDetail> handleTimeout(DependencyTimeoutException ex,
                                                       HttpServletRequest request) {
        return build(ex, HttpStatus.GATEWAY_TIMEOUT, request);
    }

    @ExceptionHandler(DependencyUnavailableException.class)
    public ResponseEntity<ProblemDetail> handleUnavailable(DependencyUnavailableException ex,
                                                           HttpServletRequest request) {
        return build(ex, HttpStatus.SERVICE_UNAVAILABLE, request);
    }

    @ExceptionHandler(DependencyException.class)
    public ResponseEntity<ProblemDetail> handleDependency(DependencyException ex,
                                                          HttpServletRequest request) {
        return build(ex, HttpStatus.SERVICE_UNAVAILABLE, request);
    }

    @ExceptionHandler(CoreException.class)
    public ResponseEntity<ProblemDetail> handleCore(CoreException ex,
                                                    HttpServletRequest request) {
        return build(ex, HttpStatus.BAD_REQUEST, request);
    }

    private ResponseEntity<ProblemDetail> build(CoreException ex,
                                                HttpStatus status,
                                                HttpServletRequest request) {
        logException(ex, status);

        ProblemDetail pd = problemDetailFactory.create(
                status,
                ex.getMessage(),
                request.getRequestURI(),
                ex.getErrorCode().getCode()
        );

        return ResponseEntity.status(status).body(pd);
    }

    private void logException(CoreException ex, HttpStatus status) {
        if (status.is5xxServerError()) {
            log.warn("[{}] {}: {}", status.value(), ex.getErrorCode().getCode(), ex.getMessage(), ex);
        } else {
            log.info("[{}] {}: {}", status.value(), ex.getErrorCode().getCode(), ex.getMessage());
        }
    }
}
