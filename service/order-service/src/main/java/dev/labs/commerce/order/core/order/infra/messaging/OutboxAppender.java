package dev.labs.commerce.order.core.order.infra.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.labs.commerce.common.event.EventPayloadConversionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;

import java.util.UUID;

/**
 * 도메인 이벤트를 현재 트랜잭션 안에서 outbox 테이블에 적재
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxAppender {

    private static final String INSERT_SQL = """
            INSERT INTO outbox_message
                (event_id, event_type, destination, partition_key, payload, created_at)
            VALUES (?, ?, ?, ?, ?::jsonb, ?)
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void append(String destination, String partitionKey, String eventType, Object payload) {
        UUID eventId = UUID.randomUUID();
        jdbcTemplate.update(
                INSERT_SQL,
                eventId,
                eventType,
                destination,
                partitionKey,
                serialize(payload),
                Timestamp.from(Instant.now())
        );
        log.debug("Outbox appended: eventId={}, type={}, key={}", eventId, eventType, partitionKey);
    }

    private String serialize(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new EventPayloadConversionException(
                    "Failed to serialize event payload: " + payload.getClass().getSimpleName(), e);
        }
    }

}
