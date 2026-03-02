package dev.labs.commerce.inventory.core.inventory.infra.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import dev.labs.commerce.common.event.EventEnvelope;
import dev.labs.commerce.common.event.EventPayloadConverter;
import dev.labs.commerce.inventory.core.inventory.application.usecase.DecreaseInventoryQuantityUseCase;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.DecreaseInventoryQuantityCommand;
import dev.labs.commerce.inventory.core.inventory.infra.messaging.dto.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class OrderEventsConsumer {

    @Bean
    public Consumer<EventEnvelope<JsonNode>> onOrderEvents(
            DecreaseInventoryQuantityUseCase decreaseInventoryQuantityUseCase,
            EventPayloadConverter eventPayloadConverter
    ) {
        return envelope -> {
            String type = envelope.meta().eventType();

            switch (type) {
                case "OrderCreatedEvent" -> {
                    OrderCreatedEvent event = eventPayloadConverter.convert(envelope.payload(), OrderCreatedEvent.class);
                    log.info("Received OrderCreatedEvent: orderId={}, itemCount={}", event.orderId(), event.items().size());
                    event.items().forEach(item ->
                            decreaseInventoryQuantityUseCase.execute(new DecreaseInventoryQuantityCommand(
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
