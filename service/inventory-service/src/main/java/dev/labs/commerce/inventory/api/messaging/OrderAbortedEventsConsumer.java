package dev.labs.commerce.inventory.api.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import dev.labs.commerce.common.event.EventEnvelope;
import dev.labs.commerce.common.event.EventPayloadConverter;
import dev.labs.commerce.inventory.api.messaging.dto.OrderAbortedEvent;
import dev.labs.commerce.inventory.core.inventory.application.usecase.RestoreInventoryQuantityUseCase;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.RestoreInventoryQuantityCommand;
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
            OrderAbortedEvent event = eventPayloadConverter.convert(envelope.payload(), OrderAbortedEvent.class);
            log.info("Received OrderAbortedEvent: orderId={}, itemCount={}", event.orderId(), event.items().size());
            event.items().forEach(item ->
                    restoreInventoryQuantityUseCase.execute(new RestoreInventoryQuantityCommand(
                            item.productId(),
                            event.orderId(),
                            item.quantity()
                    ))
            );
        };
    }
}
