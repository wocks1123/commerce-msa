package dev.labs.commerce.payment.core.payment.infra.messaging;

import dev.labs.commerce.common.event.EventPublisher;
import dev.labs.commerce.payment.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;

class OutboxRelayIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OutboxRelay outboxRelay;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private EventPublisher eventPublisher;

    @Test
    @DisplayName("PENDING row를 발행하면 PUBLISHED로 마킹되고 publish가 호출된다")
    void dispatchPending_marksPublished() {
        // given
        final String key = UUID.randomUUID().toString();
        insertPending(key);

        // when
        outboxRelay.dispatchPending();

        // then
        final Map<String, Object> result = findRowByKey(key);
        assertThat(result.get("status")).isEqualTo("PUBLISHED");
        assertThat(result.get("processed_at")).isNotNull();
        assertThat(result.get("retry_count")).isEqualTo(0);
        then(eventPublisher).should(times(1)).publish(any(), any(), any());
    }

    @Test
    @DisplayName("발행이 실패하면 PENDING으로 남고 retry_count가 증가한다")
    void dispatchPending_whenPublishFails_incrementsRetryCount() {
        // given
        final String key = UUID.randomUUID().toString();
        insertPending(key);
        willThrow(new RuntimeException("kafka down"))
                .given(eventPublisher).publish(any(), any(), any());

        // when
        outboxRelay.dispatchPending();

        // then
        final Map<String, Object> result = findRowByKey(key);
        assertThat(result.get("status")).isEqualTo("PENDING");
        assertThat(result.get("processed_at")).isNull();
        assertThat(result.get("retry_count")).isEqualTo(1);
    }

    private void insertPending(String partitionKey) {
        jdbcTemplate.update("""
                        INSERT INTO outbox_message
                            (event_id, event_type, destination, partition_key, payload, created_at)
                        VALUES (?, ?, ?, ?, ?::jsonb, ?)
                        """,
                UUID.randomUUID(),
                "PaymentApprovedEvent",
                "payment-approved-out-0",
                partitionKey,
                "{\"paymentId\":\"" + partitionKey + "\"}",
                Timestamp.from(Instant.now()));
    }

    private Map<String, Object> findRowByKey(String partitionKey) {
        return jdbcTemplate.queryForMap(
                "SELECT * FROM outbox_message WHERE partition_key = ?", partitionKey);
    }

}
