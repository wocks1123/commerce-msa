package dev.labs.commerce.order.core.order.infra.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.labs.commerce.order.core.order.application.event.OrderAbortedEvent;
import dev.labs.commerce.order.core.order.application.event.OrderExpiredEvent;
import dev.labs.commerce.order.core.order.application.event.OrderPaidEvent;
import dev.labs.commerce.order.support.AbstractIntegrationTest;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaOrderEventPublisherIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private KafkaOrderEventPublisher publisher;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.cloud.stream.kafka.binder.brokers}")
    private String bootstrapServers;

    @Test
    @DisplayName("publishOrderPaid는 트랜잭션 커밋 후 order.paid에 OrderPaidEvent envelope을 발행한다")
    void publishOrderPaid_afterCommit_publishesEnvelopeToOrderPaidTopic() {
        // given
        String orderId = UUID.randomUUID().toString();
        OrderPaidEvent event = new OrderPaidEvent(orderId, List.of(
                new OrderPaidEvent.OrderItemPayload(1L, 2)));

        try (Consumer<String, String> consumer = subscribingConsumer("order.paid")) {
            // when
            inTransaction(() -> publisher.publishOrderPaid(event));

            // then
            ConsumerRecord<String, String> record = pollForKey(consumer, orderId, Duration.ofSeconds(10));
            assertThat(record).as("expected order.paid record with key %s", orderId).isNotNull();
            JsonNode envelope = readJson(record.value());
            assertThat(envelope.path("meta").path("eventType").asText()).isEqualTo("OrderPaidEvent");
            assertThat(envelope.path("payload").path("orderId").asText()).isEqualTo(orderId);
            assertThat(envelope.path("payload").path("items")).hasSize(1);
            assertThat(envelope.path("payload").path("items").get(0).path("productId").asLong()).isEqualTo(1L);
            assertThat(envelope.path("payload").path("items").get(0).path("quantity").asInt()).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("publishOrderAborted는 트랜잭션 커밋 후 order.aborted에 OrderAbortedEvent envelope을 발행한다")
    void publishOrderAborted_afterCommit_publishesEnvelopeToOrderAbortedTopic() {
        // given
        String orderId = UUID.randomUUID().toString();
        OrderAbortedEvent event = new OrderAbortedEvent(orderId, List.of(
                new OrderAbortedEvent.OrderItemPayload(1L, 2)));

        try (Consumer<String, String> consumer = subscribingConsumer("order.aborted")) {
            // when
            inTransaction(() -> publisher.publishOrderAborted(event));

            // then
            ConsumerRecord<String, String> record = pollForKey(consumer, orderId, Duration.ofSeconds(10));
            assertThat(record).as("expected order.aborted record with key %s", orderId).isNotNull();
            JsonNode envelope = readJson(record.value());
            assertThat(envelope.path("meta").path("eventType").asText()).isEqualTo("OrderAbortedEvent");
            assertThat(envelope.path("payload").path("orderId").asText()).isEqualTo(orderId);
            assertThat(envelope.path("payload").path("items")).hasSize(1);
        }
    }

    @Test
    @DisplayName("publishOrderExpired는 트랜잭션 커밋 후 order.expired에 OrderExpiredEvent envelope을 발행한다")
    void publishOrderExpired_afterCommit_publishesEnvelopeToOrderExpiredTopic() {
        // given
        String orderId = UUID.randomUUID().toString();
        OrderExpiredEvent event = new OrderExpiredEvent(orderId, List.of(
                new OrderExpiredEvent.OrderItemPayload(1L, 2)));

        try (Consumer<String, String> consumer = subscribingConsumer("order.expired")) {
            // when
            inTransaction(() -> publisher.publishOrderExpired(event));

            // then
            ConsumerRecord<String, String> record = pollForKey(consumer, orderId, Duration.ofSeconds(10));
            assertThat(record).as("expected order.expired record with key %s", orderId).isNotNull();
            JsonNode envelope = readJson(record.value());
            assertThat(envelope.path("meta").path("eventType").asText()).isEqualTo("OrderExpiredEvent");
            assertThat(envelope.path("payload").path("orderId").asText()).isEqualTo(orderId);
            assertThat(envelope.path("payload").path("items")).hasSize(1);
        }
    }

    @Test
    @DisplayName("트랜잭션이 롤백되면 publish 호출이 있어도 메시지가 발행되지 않는다")
    void publish_whenTransactionRollsBack_doesNotPublish() {
        // given
        String orderId = UUID.randomUUID().toString();
        OrderPaidEvent event = new OrderPaidEvent(orderId, List.of(
                new OrderPaidEvent.OrderItemPayload(1L, 2)));

        try (Consumer<String, String> consumer = subscribingConsumer("order.paid")) {
            // when : 호출 후 트랜잭션을 롤백
            TransactionTemplate template = new TransactionTemplate(transactionManager);
            template.execute(status -> {
                publisher.publishOrderPaid(event);
                status.setRollbackOnly();
                return null;
            });

            // then : 2초 동안 우리 orderId의 메시지는 도착하지 않음
            ConsumerRecord<String, String> record = pollForKey(consumer, orderId, Duration.ofSeconds(2));
            assertThat(record).as("rollback 시 발행 안 되어야 함").isNull();
        }
    }

    // --- helpers ---

    private void inTransaction(Runnable body) {
        new TransactionTemplate(transactionManager).execute(status -> {
            body.run();
            return null;
        });
    }

    private ConsumerRecord<String, String> pollForKey(
            Consumer<String, String> consumer, String key, Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, String> r : records) {
                if (key.equals(r.key())) {
                    return r;
                }
            }
        }
        return null;
    }

    private Consumer<String, String> subscribingConsumer(String topic) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-listener-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<>(
                props, new StringDeserializer(), new StringDeserializer()).createConsumer();
        consumer.subscribe(List.of(topic));
        consumer.poll(Duration.ofMillis(500));
        return consumer;
    }

    private JsonNode readJson(String value) {
        try {
            return objectMapper.readTree(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
