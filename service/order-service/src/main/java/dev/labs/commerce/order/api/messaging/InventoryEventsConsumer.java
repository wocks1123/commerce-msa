package dev.labs.commerce.order.api.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import dev.labs.commerce.common.event.EventEnvelope;
import dev.labs.commerce.common.event.EventPayloadConverter;
import dev.labs.commerce.order.api.messaging.dto.StockReservationFailedEvent;
import dev.labs.commerce.order.core.order.application.usecase.CancelOrderByStockFailureUseCase;
import dev.labs.commerce.order.core.order.application.usecase.dto.CancelOrderByStockFailureCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class InventoryEventsConsumer {

    @Bean
    public Consumer<EventEnvelope<JsonNode>> onStockReservationFailed(
            CancelOrderByStockFailureUseCase cancelOrderByStockFailureUseCase,
            EventPayloadConverter eventPayloadConverter
    ) {
        return envelope -> {
            StockReservationFailedEvent event = eventPayloadConverter.convert(envelope.payload(), StockReservationFailedEvent.class);
            log.info("Received StockReservationFailedEvent: orderId={}, productId={}, errorCode={}",
                    event.orderId(), event.productId(), event.errorCode());
            cancelOrderByStockFailureUseCase.execute(new CancelOrderByStockFailureCommand(event.orderId()));
        };
    }
}
