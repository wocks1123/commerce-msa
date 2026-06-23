package dev.labs.commerce.order.core.order.infra.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.labs.commerce.common.event.EventEnvelope;
import dev.labs.commerce.common.event.EventMeta;
import dev.labs.commerce.common.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/**
 * PENDING outbox row를 폴링해 Kafka로 발행하고 PUBLISHED로 마킹한다.
 * 한 번의 dispatch는 단일 트랜잭션이며, FOR UPDATE SKIP LOCKED로 동시 인스턴스 간 중복 처리를 막는다.
 * 발행은 at-least-once다(컨슈머가 멱등이므로 허용). 발행 실패 row는 retry_count만 올리고 다음 주기에 재시도.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRelay {

    private static final int BATCH_SIZE = 100;

    private static final String FETCH_SQL = """
            SELECT id, event_id, event_type, destination, partition_key, payload, created_at
            FROM outbox_message
            WHERE status = 'PENDING'
            ORDER BY id
            LIMIT %d
            FOR UPDATE SKIP LOCKED
            """.formatted(BATCH_SIZE);

    private static final String MARK_PUBLISHED_SQL =
            "UPDATE outbox_message SET status = 'PUBLISHED', processed_at = ? WHERE id = ?";
    private static final String MARK_RETRY_SQL =
            "UPDATE outbox_message SET retry_count = retry_count + 1 WHERE id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final EventPublisher eventPublisher;

    @Transactional
    public void dispatchPending() {
        List<OutboxRow> rows = jdbcTemplate.query(FETCH_SQL, (rs, n) -> new OutboxRow(
                rs.getLong("id"),
                rs.getString("event_id"),
                rs.getString("event_type"),
                rs.getString("destination"),
                rs.getString("partition_key"),
                rs.getString("payload"),
                rs.getTimestamp("created_at").toInstant()
        ));
        if (rows.isEmpty()) {
            return;
        }

        int sent = 0;
        for (OutboxRow row : rows) {
            try {
                eventPublisher.publish(row.destination(), row.partitionKey(), toEnvelope(row));
                jdbcTemplate.update(MARK_PUBLISHED_SQL, Timestamp.from(Instant.now()), row.id());
                sent++;
            } catch (Exception e) {
                jdbcTemplate.update(MARK_RETRY_SQL, row.id());
                log.warn("Outbox dispatch failed, will retry: id={}, eventId={}", row.id(), row.eventId(), e);
            }
        }
        log.info("Outbox dispatched {}/{} messages", sent, rows.size());
    }

    private EventEnvelope<JsonNode> toEnvelope(OutboxRow row) {
        return new EventEnvelope<>(
                new EventMeta(row.eventId(), row.eventType(), row.createdAt()),
                readPayload(row.payload())
        );
    }

    private JsonNode readPayload(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse outbox payload JSON", e);
        }
    }

    private record OutboxRow(
            long id, String eventId, String eventType,
            String destination, String partitionKey, String payload, Instant createdAt) {
    }
}
