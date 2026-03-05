package dev.labs.commerce.inventory.core.inventory.infra.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import dev.labs.commerce.common.event.EventEnvelope;
import dev.labs.commerce.common.event.EventPayloadConverter;
import dev.labs.commerce.inventory.core.inventory.application.usecase.DecreaseOrderInventoryUseCase;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.DecreaseOrderInventoryCommand;
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
            DecreaseOrderInventoryUseCase decreaseOrderInventoryUseCase,
            EventPayloadConverter eventPayloadConverter
    ) {
        return envelope -> {
            OrderCreatedEvent event = eventPayloadConverter.convert(envelope.payload(), OrderCreatedEvent.class);
            log.info("Received OrderCreatedEvent: orderId={}, itemCount={}", event.orderId(), event.items().size());

            var command = new DecreaseOrderInventoryCommand(
                    event.orderId(),
                    event.items().stream()
                            .map(item -> new DecreaseOrderInventoryCommand.Item(item.productId(), item.quantity()))
                            .toList()
            );
            decreaseOrderInventoryUseCase.execute(command);
        };
    }
}
