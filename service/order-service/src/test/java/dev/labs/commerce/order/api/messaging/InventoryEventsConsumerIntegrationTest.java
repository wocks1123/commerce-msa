package dev.labs.commerce.order.api.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.labs.commerce.order.core.order.domain.OrderStatus;
import dev.labs.commerce.order.core.order.domain.SalesOrder;
import dev.labs.commerce.order.core.order.domain.SalesOrderRepository;
import dev.labs.commerce.order.core.order.domain.fixture.SalesOrderFixture;
import dev.labs.commerce.order.support.AbstractIntegrationTest;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import(InventoryEventsConsumerIntegrationTest.TestKafkaProducerConfig.class)
class InventoryEventsConsumerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("재고 예약 실패 이벤트가 도착하면 해당 주문이 ABORTED로 전이된다")
    void event_consumed_transitionsOrderToAborted() {
        SalesOrder order = salesOrderRepository.saveAndFlush(
                SalesOrderFixture.builder().withSample().build());
        sendEvent(order.getOrderId());
        awaitOrderStatus(order.getOrderId(), OrderStatus.ABORTED);
    }

    @Test
    @DisplayName("이미 ABORTED인 주문에 이벤트가 다시 도착해도 상태가 변하지 않는다")
    void alreadyAborted_remainsAborted() {
        Instant originalAbortedAt = Instant.parse("2026-01-01T00:00:00Z");
        SalesOrder order = salesOrderRepository.saveAndFlush(
                SalesOrderFixture.builder()
                        .withSample()
                        .status(OrderStatus.ABORTED)
                        .abortedAt(originalAbortedAt)
                        .build());
        sendEvent(order.getOrderId());
        Awaitility.await()
                .during(Duration.ofSeconds(2))
                .atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> {
                    SalesOrder reloaded = salesOrderRepository.findById(order.getOrderId()).orElseThrow();
                    assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.ABORTED);
                    assertThat(reloaded.getAbortedAt()).isEqualTo(originalAbortedAt);
                });
    }

    @Test
    @DisplayName("존재하지 않는 주문 이벤트가 와도 후속 정상 이벤트는 처리된다")
    void unknownOrder_doesNotBlockSubsequentMessages() {
        SalesOrder normalOrder = salesOrderRepository.saveAndFlush(
                SalesOrderFixture.builder().withSample().build());
        String unknownOrderId = "unknown-" + UUID.randomUUID();
        sendEvent(unknownOrderId);
        sendEvent(normalOrder.getOrderId());
        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    SalesOrder reloaded = salesOrderRepository.findById(normalOrder.getOrderId()).orElseThrow();
                    assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.ABORTED);
                });
    }

    private void sendEvent(String orderId) {
        kafkaTemplate.send("stock.reservation.failed", orderId, buildEnvelopeJson(orderId));
    }

    private String buildEnvelopeJson(String orderId) {
        ObjectNode envelope = objectMapper.createObjectNode();
        ObjectNode meta = envelope.putObject("meta");
        meta.put("eventId", UUID.randomUUID().toString());
        meta.put("eventType", "StockReservationFailedEvent");
        meta.put("occurredAt", Instant.now().toString());
        ObjectNode payload = envelope.putObject("payload");
        payload.put("productId", 1);
        payload.put("orderId", orderId);
        payload.put("quantity", 2);
        payload.put("errorCode", "OUT_OF_STOCK");
        return envelope.toString();
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
