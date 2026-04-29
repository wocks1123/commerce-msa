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
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
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

    @Value("${spring.cloud.stream.kafka.binder.brokers}")
    private String bootstrapServers;

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
    @DisplayName("처리 실패한 메시지는 DLT로 라우팅되고 후속 정상 메시지는 처리된다")
    void unknownOrder_routesToDltAndDoesNotBlockSubsequentMessages() {
        // given
        SalesOrder normalOrder = salesOrderRepository.saveAndFlush(
                SalesOrderFixture.builder().withSample().build());
        String unknownOrderId = "unknown-" + UUID.randomUUID();

        try (Consumer<String, String> dltConsumer = subscribingConsumer("stock.reservation.failed.DLT")) {
            // when : 잘못된 메시지를 먼저, 정상 메시지를 뒤에 발송
            sendEvent(unknownOrderId);
            sendEvent(normalOrder.getOrderId());

            // then : 정상 주문은 결국 ABORTED로 전이됨 (재시도 backoff 후)
            Awaitility.await()
                    .atMost(Duration.ofSeconds(30))
                    .pollInterval(Duration.ofMillis(500))
                    .untilAsserted(() -> {
                        SalesOrder reloaded = salesOrderRepository.findById(normalOrder.getOrderId()).orElseThrow();
                        assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.ABORTED);
                    });

            // then : 잘못된 메시지는 DLT 토픽에 페이로드 그대로 라우팅됨
            ConsumerRecord<String, String> dltRecord = pollForKey(dltConsumer, unknownOrderId, Duration.ofSeconds(10));
            assertThat(dltRecord).as("DLT에 unknown 메시지가 라우팅되어야 함").isNotNull();
            JsonNode envelope = objectMapper.readTree(dltRecord.value());
            assertThat(envelope.path("payload").path("orderId").asText()).isEqualTo(unknownOrderId);
            assertThat(envelope.path("meta").path("eventType").asText()).isEqualTo("StockReservationFailedEvent");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
