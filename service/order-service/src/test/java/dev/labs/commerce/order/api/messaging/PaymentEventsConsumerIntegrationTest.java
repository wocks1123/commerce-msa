package dev.labs.commerce.order.api.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.labs.commerce.order.core.order.domain.OrderStatus;
import dev.labs.commerce.order.core.order.domain.SalesOrder;
import dev.labs.commerce.order.core.order.domain.SalesOrderRepository;
import dev.labs.commerce.order.core.order.domain.fixture.SalesOrderFixture;
import dev.labs.commerce.order.support.AbstractIntegrationTest;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import(PaymentEventsConsumerIntegrationTest.TestKafkaProducerConfig.class)
class PaymentEventsConsumerIntegrationTest extends AbstractIntegrationTest {

    private static final long CUSTOMER_ID = 100L;
    private static final int QUANTITY = 2;
    private static final String CURRENCY = "KRW";

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.cloud.stream.kafka.binder.brokers}")
    private String bootstrapServers;

    @Nested
    @DisplayName("결제 초기화 이벤트 수신")
    class PaymentInitialized {

        @Test
        @DisplayName("주문이 CREATED 상태일 때 결제 초기화 이벤트가 도착하면 PENDING으로 전이된다")
        void paymentInitializedEvent_consumed_transitionsCreatedOrderToPending() {
            // given
            SalesOrder order = saveCreatedOrder();
            ObjectNode payload = objectMapper.createObjectNode()
                    .put("paymentId", "pay-" + UUID.randomUUID())
                    .put("orderId", order.getOrderId())
                    .put("requestedAt", Instant.now().toString());

            // when
            sendEnvelope("payment.initialized", "PaymentInitializedEvent", payload, order.getOrderId());

            // then
            awaitOrderStatus(order.getOrderId(), OrderStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("결제 승인 이벤트 수신")
    class PaymentApproved {

        @Test
        @DisplayName("PENDING 주문에 결제 승인 이벤트가 도착하면 PAID로 전이되고 order.paid가 발행된다")
        void paymentApprovedEvent_consumed_transitionsToPaidAndPublishesOrderPaid() {
            // given
            SalesOrder order = savePendingOrder();
            ObjectNode payload = objectMapper.createObjectNode()
                    .put("orderId", order.getOrderId())
                    .put("customerId", CUSTOMER_ID)
                    .put("amount", order.getTotalPrice())
                    .put("currency", CURRENCY);

            try (Consumer<String, String> orderPaidConsumer = subscribingConsumer("order.paid")) {
                // when
                sendEnvelope("payment.approved", "PaymentApprovedEvent", payload, order.getOrderId());

                // then : DB 상태
                awaitOrderStatus(order.getOrderId(), OrderStatus.PAID);

                // then : outbound order.paid 발행
                ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(
                        orderPaidConsumer, "order.paid", Duration.ofSeconds(10));
                assertThat(record.key()).isEqualTo(order.getOrderId());

                JsonNode envelope = readJson(record.value());
                assertThat(envelope.path("meta").path("eventType").asText()).isEqualTo("OrderPaidEvent");
                assertThat(envelope.path("payload").path("orderId").asText()).isEqualTo(order.getOrderId());
                assertThat(envelope.path("payload").path("items")).hasSize(1);
                assertThat(envelope.path("payload").path("items").get(0).path("quantity").asInt()).isEqualTo(QUANTITY);
            }
        }
    }

    @Nested
    @DisplayName("결제 실패 이벤트 수신")
    class PaymentFailed {

        @Test
        @DisplayName("PENDING 주문에 결제 실패 이벤트가 도착하면 ABORTED로 전이된다")
        void paymentFailedEvent_consumed_transitionsToAborted() {
            // given
            SalesOrder order = savePendingOrder();
            ObjectNode payload = objectMapper.createObjectNode()
                    .put("orderId", order.getOrderId())
                    .put("failureCode", "PG_DECLINED");

            // when
            sendEnvelope("payment.failed", "PaymentFailedEvent", payload, order.getOrderId());

            // then
            awaitOrderStatus(order.getOrderId(), OrderStatus.ABORTED);
        }
    }

    @Nested
    @DisplayName("결제 만료 이벤트 수신")
    class PaymentExpired {

        @Test
        @DisplayName("PENDING 주문에 결제 만료 이벤트가 도착하면 EXPIRED로 전이된다")
        void paymentExpiredEvent_consumed_transitionsToExpired() {
            // given
            SalesOrder order = savePendingOrder();
            ObjectNode payload = objectMapper.createObjectNode().put("orderId", order.getOrderId());

            // when
            sendEnvelope("payment.expired", "PaymentExpiredEvent", payload, order.getOrderId());

            // then
            awaitOrderStatus(order.getOrderId(), OrderStatus.EXPIRED);
        }
    }

    // --- helpers ---

    private SalesOrder saveCreatedOrder() {
        return salesOrderRepository.saveAndFlush(
                SalesOrderFixture.builder().withSample().build());
    }

    private SalesOrder savePendingOrder() {
        return salesOrderRepository.saveAndFlush(
                SalesOrderFixture.builder()
                        .withSample()
                        .status(OrderStatus.PENDING)
                        .pendingAt(Instant.now())
                        .build());
    }

    private void sendEnvelope(String topic, String eventType, ObjectNode payload, String key) {
        ObjectNode envelope = objectMapper.createObjectNode();
        ObjectNode meta = envelope.putObject("meta");
        meta.put("eventId", UUID.randomUUID().toString());
        meta.put("eventType", eventType);
        meta.put("occurredAt", Instant.now().toString());
        envelope.set("payload", payload);
        kafkaTemplate.send(topic, key, envelope.toString());
    }

    private void awaitOrderStatus(String orderId, OrderStatus expected) {
        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> {
                    SalesOrder reloaded = salesOrderRepository.findById(orderId).orElseThrow();
                    assertThat(reloaded.getStatus()).isEqualTo(expected);
                });
    }

    private Consumer<String, String> subscribingConsumer(String topic) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-listener-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
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

    @TestConfiguration(proxyBeanMethods = false)
    static class TestKafkaProducerConfig {

        @Bean
        KafkaTemplate<String, String> testKafkaTemplate(
                @Value("${spring.cloud.stream.kafka.binder.brokers}") String bootstrapServers) {
            Map<String, Object> config = new HashMap<>();
            config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(config));
        }
    }
}
