package dev.labs.commerce.inventory.api.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.labs.commerce.inventory.core.inventory.domain.Inventory;
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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

@Import(ProductEventsConsumerIntegrationTest.TestKafkaProducerConfig.class)
class ProductEventsConsumerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("ProductRegisteredEvent가 도착하면 inventory row가 생성된다")
    void event_consumed_createsInventoryRow() {
        long productId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);

        sendEvent(productId);

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> {
                    Optional<Inventory> inventory = inventoryRepository.findById(productId);
                    assertThat(inventory).isPresent();
                    assertThat(inventory.get().getTotalQuantity()).isZero();
                    assertThat(inventory.get().getReservedQuantity()).isZero();
                });
    }

    @Test
    @DisplayName("이미 등록된 productId로 ProductRegisteredEvent가 다시 도착해도 inventory 상태가 변하지 않는다")
    void duplicateProductId_idempotentNoOp() {
        long productId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
        Inventory existing = Inventory.create(productId);
        existing.increase(7);
        inventoryRepository.saveAndFlush(existing);

        sendEvent(productId);

        Awaitility.await()
                .during(Duration.ofSeconds(2))
                .atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> {
                    Inventory reloaded = inventoryRepository.findById(productId).orElseThrow();
                    assertThat(reloaded.getTotalQuantity()).isEqualTo(7);
                    assertThat(reloaded.getReservedQuantity()).isZero();
                });
    }

    private void sendEvent(long productId) {
        kafkaTemplate.send("product.registered", String.valueOf(productId), buildEnvelopeJson(productId));
    }

    private String buildEnvelopeJson(long productId) {
        ObjectNode envelope = objectMapper.createObjectNode();
        ObjectNode meta = envelope.putObject("meta");
        meta.put("eventId", UUID.randomUUID().toString());
        meta.put("eventType", "ProductRegisteredEvent");
        meta.put("occurredAt", Instant.now().toString());
        ObjectNode payload = envelope.putObject("payload");
        payload.put("productId", productId);
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
