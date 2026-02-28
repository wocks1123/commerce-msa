package dev.labs.commerce.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EventPayloadConverter {

    private final ObjectMapper objectMapper;

    /**
     * payload(JsonNode 등)를 지정한 클래스 타입으로 변환한다.
     *
     * @param payload     변환할 원본 데이터
     * @param targetClass 변환할 대상 클래스 타입
     * @param <T>         대상 타입
     * @return 변환된 객체
     * @throws EventPayloadConversionException 변환 실패 시 발생
     */
    public <T> T convert(Object payload, Class<T> targetClass) {
        try {
            return objectMapper.convertValue(payload, targetClass);
        } catch (IllegalArgumentException e) {
            throw new EventPayloadConversionException(
                    String.format("Failed to convert event payload to %s", targetClass.getSimpleName()),
                    e
            );
        }
    }
}
