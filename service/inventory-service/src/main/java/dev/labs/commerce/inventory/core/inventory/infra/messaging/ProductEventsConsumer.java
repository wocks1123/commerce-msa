package dev.labs.commerce.inventory.core.inventory.infra.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import dev.labs.commerce.common.event.EventEnvelope;
import dev.labs.commerce.common.event.EventPayloadConverter;
import dev.labs.commerce.inventory.core.inventory.application.usecase.RegisterInventoryUseCase;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.RegisterInventoryCommand;
import dev.labs.commerce.inventory.core.inventory.infra.messaging.dto.ProductRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class ProductEventsConsumer {

    @Bean
    public Consumer<EventEnvelope<JsonNode>> onProductEvents(
            RegisterInventoryUseCase registerInventoryUseCase,
            EventPayloadConverter eventPayloadConverter
    ) {
        return envelope -> {
            String type = envelope.meta().eventType();

            switch (type) {
                case "ProductRegisteredEvent" -> {
                    ProductRegisteredEvent event = eventPayloadConverter.convert(envelope.payload(), ProductRegisteredEvent.class);
                    registerInventoryUseCase.execute(new RegisterInventoryCommand(event.productId()));
                }
                default -> log.warn("Unhandled event type: {}", type);
            }
        };
    }
}
