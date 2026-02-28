package dev.labs.commerce.common.web.advice;

import dev.labs.commerce.common.web.problem.ProblemDetailFactory;
import dev.labs.commerce.common.web.problem.ValidationErrorMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(2)
public class ValidationExceptionAdvice {

    private final ProblemDetailFactory problemDetailFactory;
    private final ValidationErrorMapper validationErrorMapper;

    public ValidationExceptionAdvice(ProblemDetailFactory problemDetailFactory,
                                     ValidationErrorMapper validationErrorMapper) {
        this.problemDetailFactory = problemDetailFactory;
        this.validationErrorMapper = validationErrorMapper;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        var errors = validationErrorMapper.from(ex);

        ProblemDetail pd = problemDetailFactory.create(
                HttpStatus.BAD_REQUEST,
                "Invalid request parameters.",
                request.getRequestURI(),
                "VALIDATION_ERROR",
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }
}
