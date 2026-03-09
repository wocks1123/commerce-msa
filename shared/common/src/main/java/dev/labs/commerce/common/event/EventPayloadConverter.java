package dev.labs.commerce.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class EventPayloadConverter {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    /**
     * payload(JsonNode 등)를 지정한 클래스 타입으로 변환하고 Bean Validation을 수행한다.
     *
     * @param payload     변환할 원본 데이터
     * @param targetClass 변환할 대상 클래스 타입
     * @param <T>         대상 타입
     * @return 변환된 객체
     * @throws EventPayloadConversionException 변환 실패 또는 검증 실패 시 발생
     */
    public <T> T convert(Object payload, Class<T> targetClass) {
        T converted;
        try {
            converted = objectMapper.convertValue(payload, targetClass);
        } catch (IllegalArgumentException e) {
            throw new EventPayloadConversionException(
                    String.format("Failed to convert event payload to %s", targetClass.getSimpleName()),
                    e
            );
        }

        Set<ConstraintViolation<T>> violations = validator.validate(converted);
        if (!violations.isEmpty()) {
            String details = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            throw new EventPayloadConversionException(
                    String.format("Event payload validation failed for %s — %s", targetClass.getSimpleName(), details)
            );
        }

        return converted;
    }
}
