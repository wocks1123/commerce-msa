package dev.labs.commerce.common.web.advice;

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
@Order(999)
@Slf4j
public class FallbackExceptionAdvice {

    private final ProblemDetailFactory problemDetailFactory;

    public FallbackExceptionAdvice(ProblemDetailFactory problemDetailFactory) {
        this.problemDetailFactory = problemDetailFactory;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnknown(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unhandled exception at {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);

        ProblemDetail pd = problemDetailFactory.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred.",
                request.getRequestURI(),
                "INTERNAL_ERROR"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }
}
