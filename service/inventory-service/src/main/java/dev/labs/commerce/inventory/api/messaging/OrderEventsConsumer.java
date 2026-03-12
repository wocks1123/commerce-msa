package dev.labs.commerce.inventory.api.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import dev.labs.commerce.common.event.EventEnvelope;
import dev.labs.commerce.common.event.EventPayloadConverter;
import dev.labs.commerce.inventory.api.messaging.dto.OrderCreatedEvent;
import dev.labs.commerce.inventory.core.inventory.application.usecase.ReserveOrderInventoryUseCase;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.ReserveOrderInventoryCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class OrderEventsConsumer {

    @Bean
    public Consumer<EventEnvelope<JsonNode>> onOrderEvents(
            ReserveOrderInventoryUseCase reserveOrderInventoryUseCase,
            EventPayloadConverter eventPayloadConverter
    ) {
        return envelope -> {
            OrderCreatedEvent event = eventPayloadConverter.convert(envelope.payload(), OrderCreatedEvent.class);
            log.info("Received OrderCreatedEvent: orderId={}, itemCount={}", event.orderId(), event.items().size());

            var command = new ReserveOrderInventoryCommand(
                    event.orderId(),
                    event.items().stream()
                            .map(item -> new ReserveOrderInventoryCommand.Item(item.productId(), item.quantity()))
                            .toList()
            );
            reserveOrderInventoryUseCase.execute(command);
        };
    }
}
