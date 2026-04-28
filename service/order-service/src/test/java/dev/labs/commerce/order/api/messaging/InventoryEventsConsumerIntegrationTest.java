package dev.labs.commerce.order.api.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.labs.commerce.order.core.order.domain.OrderItem;
import dev.labs.commerce.order.core.order.domain.OrderStatus;
import dev.labs.commerce.order.core.order.domain.SalesOrder;
import dev.labs.commerce.order.core.order.domain.SalesOrderRepository;
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
import java.util.List;
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
    void stockReservationFailedEvent_consumed_transitionsOrderToAborted() {
        // given
        OrderItem item = OrderItem.create(1L, "상품A", 5000L, 2, "KRW");
        SalesOrder order = SalesOrder.create(100L, "KRW", List.of(item), Instant.now());
        salesOrderRepository.saveAndFlush(order);

        String envelopeJson = buildEnvelopeJson(order.getOrderId());

        // when
        kafkaTemplate.send("stock.reservation.failed", order.getOrderId(), envelopeJson);

        // then
        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> {
                    SalesOrder reloaded = salesOrderRepository.findById(order.getOrderId()).orElseThrow();
                    assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.ABORTED);
                    assertThat(reloaded.getAbortedAt()).isNotNull();
                });
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
