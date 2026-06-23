package dev.labs.commerce.order.core.order.infra.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.labs.commerce.order.core.order.application.event.OrderAbortedEvent;
import dev.labs.commerce.order.core.order.application.event.OrderExpiredEvent;
import dev.labs.commerce.order.core.order.application.event.OrderPaidEvent;
import dev.labs.commerce.order.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutboxOrderEventPublisherIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OutboxOrderEventPublisher publisher;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("OrderPaidEvent를 PENDING 상태의 outbox row로 적재한다")
    void publishOrderPaid_appendsPendingOutboxRow() {
        // given
        String orderId = UUID.randomUUID().toString();
        OrderPaidEvent event = new OrderPaidEvent(orderId, List.of(
                new OrderPaidEvent.OrderItemPayload(1L, 2)));

        // when
        inTransaction(() -> publisher.publishOrderPaid(event));

        // then
        Map<String, Object> row = findRowByKey(orderId);
        assertThat(row.get("destination")).isEqualTo("order-paid-out-0");
        assertThat(row.get("event_type")).isEqualTo("OrderPaidEvent");
        assertThat(row.get("partition_key")).isEqualTo(orderId);
        assertThat(row.get("status")).isEqualTo("PENDING");
        assertThat(row.get("retry_count")).isEqualTo(0);
        assertThat(row.get("processed_at")).isNull();
        assertThat(row.get("event_id")).isNotNull();
    }

    @Test
    @DisplayName("OrderAbortedEvent를 해당 destination·event type으로 적재한다")
    void publishOrderAborted_appendsRowWithAbortedDestinationAndType() {
        // given
        String orderId = UUID.randomUUID().toString();
        OrderAbortedEvent event = new OrderAbortedEvent(orderId, List.of(
                new OrderAbortedEvent.OrderItemPayload(1L, 2)));

        // when
        inTransaction(() -> publisher.publishOrderAborted(event));

        // then
        Map<String, Object> row = findRowByKey(orderId);
        assertThat(row.get("destination")).isEqualTo("order-aborted-out-0");
        assertThat(row.get("event_type")).isEqualTo("OrderAbortedEvent");
    }

    @Test
    @DisplayName("OrderExpiredEvent를 해당 destination·event type으로 적재한다")
    void publishOrderExpired_appendsRowWithExpiredDestinationAndType() {
        // given
        String orderId = UUID.randomUUID().toString();
        OrderExpiredEvent event = new OrderExpiredEvent(orderId, List.of(
                new OrderExpiredEvent.OrderItemPayload(1L, 2)));

        // when
        inTransaction(() -> publisher.publishOrderExpired(event));

        // then
        Map<String, Object> row = findRowByKey(orderId);
        assertThat(row.get("destination")).isEqualTo("order-expired-out-0");
        assertThat(row.get("event_type")).isEqualTo("OrderExpiredEvent");
    }

    @Test
    @DisplayName("적재된 payload에 orderId와 items가 정확히 직렬화된다")
    void append_serializesOrderIdAndItemsIntoPayload() {
        // given
        String orderId = UUID.randomUUID().toString();
        OrderPaidEvent event = new OrderPaidEvent(orderId, List.of(
                new OrderPaidEvent.OrderItemPayload(1L, 2)));

        // when
        inTransaction(() -> publisher.publishOrderPaid(event));

        // then
        JsonNode payload = readPayload(findRowByKey(orderId));
        assertThat(payload.path("orderId").asText()).isEqualTo(orderId);
        assertThat(payload.path("items")).hasSize(1);
        assertThat(payload.path("items").get(0).path("productId").asLong()).isEqualTo(1L);
        assertThat(payload.path("items").get(0).path("quantity").asInt()).isEqualTo(2);
    }

    @Test
    @DisplayName("트랜잭션 없이 호출하면 예외가 발생하고 row가 적재되지 않는다")
    void publish_withoutTransaction_throwsAndAppendsNothing() {
        // given
        String orderId = UUID.randomUUID().toString();
        OrderPaidEvent event = new OrderPaidEvent(orderId, List.of(
                new OrderPaidEvent.OrderItemPayload(1L, 2)));

        // when
        assertThatThrownBy(() -> publisher.publishOrderPaid(event))
                .isInstanceOf(IllegalTransactionStateException.class);

        // then
        assertThat(countRowsByKey(orderId)).isEqualTo(0);
    }

    @Test
    @DisplayName("호출 후 트랜잭션이 롤백되면 row가 적재되지 않는다")
    void publish_whenTransactionRollsBack_appendsNothing() {
        // given
        String orderId = UUID.randomUUID().toString();
        OrderPaidEvent event = new OrderPaidEvent(orderId, List.of(
                new OrderPaidEvent.OrderItemPayload(1L, 2)));

        // when
        new TransactionTemplate(transactionManager).execute(status -> {
            publisher.publishOrderPaid(event);
            status.setRollbackOnly();
            return null;
        });

        // then
        assertThat(countRowsByKey(orderId)).isEqualTo(0);
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
