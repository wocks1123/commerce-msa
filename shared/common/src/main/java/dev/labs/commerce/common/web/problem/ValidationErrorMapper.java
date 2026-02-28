package dev.labs.commerce.common.web.problem;

import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ValidationErrorMapper {

    /**
     * Bean Validation 오류를 ProblemDetail properties에 넣기 좋은 형태로 변환한다.
     * <p>
     * output example:
     * [
     * {"field": "price", "reason": "must be greater than 0"},
     * {"field": "productName", "reason": "must not be blank"}
     * ]
     */
    public List<Map<String, String>> from(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = new ArrayList<>();

        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("field", fe.getField());
            item.put("reason", defaultReason(fe));
            errors.add(item);
        }

        return errors;
    }

    private String defaultReason(FieldError fe) {
        String msg = fe.getDefaultMessage();
        return (msg == null || msg.isBlank()) ? "invalid" : msg;
    }
}
