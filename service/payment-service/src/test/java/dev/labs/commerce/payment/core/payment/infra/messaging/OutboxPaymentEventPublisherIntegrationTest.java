package dev.labs.commerce.payment.core.payment.infra.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.labs.commerce.payment.core.payment.application.event.PaymentApprovedEvent;
import dev.labs.commerce.payment.core.payment.application.event.PaymentExpiredEvent;
import dev.labs.commerce.payment.core.payment.application.event.PaymentInitializedEvent;
import dev.labs.commerce.payment.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutboxPaymentEventPublisherIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OutboxPaymentEventPublisher publisher;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("PaymentInitializedEvent는 PENDING으로 적재되고 partition_key는 orderId다")
    void publishPaymentInitialized_appendsPendingRowKeyedByOrderId() {
        // given
        final String paymentId = "pay-" + UUID.randomUUID();
        final String orderId = UUID.randomUUID().toString();
        final PaymentInitializedEvent event = new PaymentInitializedEvent(paymentId, orderId, Instant.now());

        // when
        inTransaction(() -> publisher.publishPaymentInitialized(event));

        // then
        final Map<String, Object> result = findRowByKey(orderId);
        assertThat(result.get("destination")).isEqualTo("payment-initialized-out-0");
        assertThat(result.get("event_type")).isEqualTo("PaymentInitializedEvent");
        assertThat(result.get("partition_key")).isEqualTo(orderId);
        assertThat(result.get("status")).isEqualTo("PENDING");
        assertThat(result.get("retry_count")).isEqualTo(0);
        assertThat(result.get("processed_at")).isNull();
        assertThat(result.get("event_id")).isNotNull();
    }

    @Test
    @DisplayName("PaymentApprovedEvent는 payment-approved-out-0로 적재되고 partition_key는 paymentId다")
    void publishPaymentApproved_appendsRowKeyedByPaymentId() {
        // given
        final String paymentId = "pay-" + UUID.randomUUID();
        final String orderId = UUID.randomUUID().toString();
        final PaymentApprovedEvent event = new PaymentApprovedEvent(
                paymentId, orderId, 1L, 1000L, "KRW", Instant.now());

        // when
        inTransaction(() -> publisher.publishPaymentApproved(event));

        // then
        final Map<String, Object> result = findRowByKey(paymentId);
        assertThat(result.get("destination")).isEqualTo("payment-approved-out-0");
        assertThat(result.get("event_type")).isEqualTo("PaymentApprovedEvent");
        assertThat(result.get("partition_key")).isEqualTo(paymentId);
    }

    @Test
    @DisplayName("PaymentExpiredEvent는 payment-expired-out-0로 적재되고 partition_key는 paymentId다")
    void publishPaymentExpired_appendsRowKeyedByPaymentId() {
        // given
        final String paymentId = "pay-" + UUID.randomUUID();
        final String orderId = UUID.randomUUID().toString();
        final PaymentExpiredEvent event = new PaymentExpiredEvent(paymentId, orderId, 1L, Instant.now());

        // when
        inTransaction(() -> publisher.publishPaymentExpired(event));

        // then
        final Map<String, Object> result = findRowByKey(paymentId);
        assertThat(result.get("destination")).isEqualTo("payment-expired-out-0");
        assertThat(result.get("event_type")).isEqualTo("PaymentExpiredEvent");
        assertThat(result.get("partition_key")).isEqualTo(paymentId);
    }

    @Test
    @DisplayName("PaymentApprovedEvent payload의 scalar 필드가 정확히 직렬화된다")
    void publishPaymentApproved_serializesScalarFields() {
        // given
        final String paymentId = "pay-" + UUID.randomUUID();
        final String orderId = UUID.randomUUID().toString();
        final PaymentApprovedEvent event = new PaymentApprovedEvent(
                paymentId, orderId, 42L, 1500L, "KRW", Instant.now());

        // when
        inTransaction(() -> publisher.publishPaymentApproved(event));

        // then
        final JsonNode payload = readPayload(findRowByKey(paymentId));
        assertThat(payload.path("paymentId").asText()).isEqualTo(paymentId);
        assertThat(payload.path("orderId").asText()).isEqualTo(orderId);
        assertThat(payload.path("customerId").asLong()).isEqualTo(42L);
        assertThat(payload.path("amount").asLong()).isEqualTo(1500L);
        assertThat(payload.path("currency").asText()).isEqualTo("KRW");
    }

    @Test
    @DisplayName("트랜잭션 없이 호출하면 예외가 발생하고 row가 적재되지 않는다")
    void publish_withoutTransaction_throwsAndAppendsNothing() {
        // given
        final String paymentId = "pay-" + UUID.randomUUID();
        final String orderId = UUID.randomUUID().toString();
        final PaymentApprovedEvent event = new PaymentApprovedEvent(
                paymentId, orderId, 1L, 1000L, "KRW", Instant.now());

        // when
        assertThatThrownBy(() -> publisher.publishPaymentApproved(event))
                .isInstanceOf(IllegalTransactionStateException.class);

        // then
        assertThat(countRowsByKey(paymentId)).isEqualTo(0);
    }

    @Test
    @DisplayName("호출 후 트랜잭션이 롤백되면 row가 적재되지 않는다")
    void publish_whenTransactionRollsBack_appendsNothing() {
        // given
        final String paymentId = "pay-" + UUID.randomUUID();
        final String orderId = UUID.randomUUID().toString();
        final PaymentApprovedEvent event = new PaymentApprovedEvent(
                paymentId, orderId, 1L, 1000L, "KRW", Instant.now());

        // when
        new TransactionTemplate(transactionManager).execute(status -> {
            publisher.publishPaymentApproved(event);
            status.setRollbackOnly();
            return null;
        });

        // then
        assertThat(countRowsByKey(paymentId)).isEqualTo(0);
    }

    // --- helpers ---

    private void inTransaction(Runnable body) {
        new TransactionTemplate(transactionManager).execute(status -> {
            body.run();
            return null;
        });
    }

    private Map<String, Object> findRowByKey(String partitionKey) {
        return jdbcTemplate.queryForMap(
                "SELECT * FROM outbox_message WHERE partition_key = ?", partitionKey);
    }

    private int countRowsByKey(String partitionKey) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM outbox_message WHERE partition_key = ?", Integer.class, partitionKey);
        return count == null ? 0 : count;
    }

    private JsonNode readPayload(Map<String, Object> row) {
        try {
            return objectMapper.readTree(row.get("payload").toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
