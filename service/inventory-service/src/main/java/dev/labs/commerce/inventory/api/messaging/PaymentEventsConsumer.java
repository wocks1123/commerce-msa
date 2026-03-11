package dev.labs.commerce.inventory.api.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import dev.labs.commerce.common.event.EventEnvelope;
import dev.labs.commerce.common.event.EventPayloadConverter;
import dev.labs.commerce.inventory.api.messaging.dto.OrderPaidEvent;
import dev.labs.commerce.inventory.core.inventory.application.usecase.ConfirmOrderInventoryUseCase;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.ConfirmOrderInventoryCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class PaymentEventsConsumer {

    @Bean
    public Consumer<EventEnvelope<JsonNode>> onOrderPaidEvents(
            ConfirmOrderInventoryUseCase confirmOrderInventoryUseCase,
            EventPayloadConverter eventPayloadConverter
    ) {
        return envelope -> {
            OrderPaidEvent event = eventPayloadConverter.convert(envelope.payload(), OrderPaidEvent.class);
            log.info("Received OrderPaidEvent: orderId={}, itemCount={}", event.orderId(), event.items().size());

            var command = new ConfirmOrderInventoryCommand(
                    event.orderId(),
                    event.items().stream()
                            .map(item -> new ConfirmOrderInventoryCommand.Item(item.productId(), item.quantity()))
                            .toList()
            );
            confirmOrderInventoryUseCase.execute(command);
        };
    }
}
