package dev.labs.commerce.inventory.core.inventory.infra.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import dev.labs.commerce.common.event.EventEnvelope;
import dev.labs.commerce.common.event.EventPayloadConverter;
import dev.labs.commerce.inventory.core.inventory.application.usecase.RestoreInventoryQuantityUseCase;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.RestoreInventoryQuantityCommand;
import dev.labs.commerce.inventory.core.inventory.infra.messaging.dto.OrderAbortedKafkaEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class OrderAbortedEventsConsumer {

    @Bean
    public Consumer<EventEnvelope<JsonNode>> onOrderAbortedEvents(
            RestoreInventoryQuantityUseCase restoreInventoryQuantityUseCase,
            EventPayloadConverter eventPayloadConverter
    ) {
        return envelope -> {
            String type = envelope.meta().eventType();

            switch (type) {
                case "OrderAbortedEvent" -> {
                    OrderAbortedKafkaEvent event = eventPayloadConverter.convert(envelope.payload(), OrderAbortedKafkaEvent.class);
                    log.info("Received OrderAbortedEvent: orderId={}, itemCount={}", event.orderId(), event.items().size());
                    event.items().forEach(item ->
                            restoreInventoryQuantityUseCase.execute(new RestoreInventoryQuantityCommand(
                                    item.productId(),
                                    event.orderId(),
                                    item.quantity()
                            ))
                    );
                }
                default -> log.warn("Unhandled event type: {}", type);
            }
        };
    }
}
