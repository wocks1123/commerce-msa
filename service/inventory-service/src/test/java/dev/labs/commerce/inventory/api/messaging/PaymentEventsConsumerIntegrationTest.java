package dev.labs.commerce.inventory.api.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.labs.commerce.inventory.core.inventory.domain.Actor;
import dev.labs.commerce.inventory.core.inventory.domain.Inventory;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryHistory;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryHistoryRepository;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryRepository;
import dev.labs.commerce.inventory.support.AbstractIntegrationTest;
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
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

@Import(PaymentEventsConsumerIntegrationTest.TestKafkaProducerConfig.class)
class PaymentEventsConsumerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryHistoryRepository inventoryHistoryRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("OrderPaidEvent가 도착하면 예약된 재고가 확정된다 (reserved/total 차감)")
    void event_consumed_confirmsReservedStock() {
        long productId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
        Inventory inventory = Inventory.create(productId);
        inventory.increase(10);
        inventory.reserve(5);
        inventoryRepository.saveAndFlush(inventory);

        String orderId = UUID.randomUUID().toString();
        sendEvent(orderId, productId, 5);

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> {
                    Inventory reloaded = inventoryRepository.findById(productId).orElseThrow();
                    assertThat(reloaded.getTotalQuantity()).isEqualTo(5);
                    assertThat(reloaded.getReservedQuantity()).isZero();
                });
    }

    @Test
    @DisplayName("이미 CONFIRM된 (orderId, productId)에 OrderPaidEvent가 다시 도착해도 상태가 변하지 않는다")
    void alreadyConfirmed_idempotentNoOp() {
        long productId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
        String orderId = UUID.randomUUID().toString();

        Inventory inventory = Inventory.create(productId);
        inventory.increase(10);
        inventory.reserve(5);
        inventory.confirm(5);
        inventoryRepository.saveAndFlush(inventory);
        inventoryHistoryRepository.saveAndFlush(
                InventoryHistory.confirm(orderId, inventory, 5, Actor.ORDER_SERVICE));

        sendEvent(orderId, productId, 5);

        Awaitility.await()
                .during(Duration.ofSeconds(2))
                .atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> {
                    Inventory reloaded = inventoryRepository.findById(productId).orElseThrow();
                    assertThat(reloaded.getTotalQuantity()).isEqualTo(5);
                    assertThat(reloaded.getReservedQuantity()).isZero();
                });
    }

    private void sendEvent(String orderId, long productId, int quantity) {
        kafkaTemplate.send("order.paid", orderId, buildEnvelopeJson(orderId, productId, quantity));
    }

    private String buildEnvelopeJson(String orderId, long productId, int quantity) {
        ObjectNode envelope = objectMapper.createObjectNode();
        ObjectNode meta = envelope.putObject("meta");
        meta.put("eventId", UUID.randomUUID().toString());
        meta.put("eventType", "OrderPaidEvent");
        meta.put("occurredAt", Instant.now().toString());
        ObjectNode payload = envelope.putObject("payload");
        payload.put("orderId", orderId);
        ArrayNode items = payload.putArray("items");
        ObjectNode item = items.addObject();
        item.put("productId", productId);
        item.put("quantity", quantity);
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
