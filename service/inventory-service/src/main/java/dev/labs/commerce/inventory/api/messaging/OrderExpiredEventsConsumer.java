package dev.labs.commerce.inventory.api.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import dev.labs.commerce.common.event.EventEnvelope;
import dev.labs.commerce.common.event.EventPayloadConverter;
import dev.labs.commerce.inventory.api.messaging.dto.OrderExpiredEvent;
import dev.labs.commerce.inventory.core.inventory.application.usecase.ReleaseOrderInventoryUseCase;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.ReleaseOrderInventoryCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class OrderExpiredEventsConsumer {

    @Bean
    public Consumer<EventEnvelope<JsonNode>> onOrderExpiredEvents(
            ReleaseOrderInventoryUseCase releaseOrderInventoryUseCase,
            EventPayloadConverter eventPayloadConverter
    ) {
        return envelope -> {
            OrderExpiredEvent event = eventPayloadConverter.convert(envelope.payload(), OrderExpiredEvent.class);
            log.info("Received OrderExpiredEvent: orderId={}, itemCount={}", event.orderId(), event.items().size());
            event.items().forEach(item ->
                    releaseOrderInventoryUseCase.execute(new ReleaseOrderInventoryCommand(
                            item.productId(),
                            event.orderId(),
                            item.quantity()
                    ))
            );
        };
    }
}
