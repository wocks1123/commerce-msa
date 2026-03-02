package dev.labs.commerce.order.core.order.infra.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import dev.labs.commerce.common.event.EventEnvelope;
import dev.labs.commerce.common.event.EventPayloadConverter;
import dev.labs.commerce.order.core.order.application.usecase.CancelOrderByStockFailureUseCase;
import dev.labs.commerce.order.core.order.application.usecase.ConfirmStockDeductedUseCase;
import dev.labs.commerce.order.core.order.application.usecase.dto.CancelOrderByStockFailureCommand;
import dev.labs.commerce.order.core.order.application.usecase.dto.ConfirmStockDeductedCommand;
import dev.labs.commerce.order.core.order.infra.messaging.dto.StockDeductedKafkaEvent;
import dev.labs.commerce.order.core.order.infra.messaging.dto.StockDeductionFailedKafkaEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class InventoryEventsConsumer {

    @Bean
    public Consumer<EventEnvelope<JsonNode>> onStockDeducted(
            ConfirmStockDeductedUseCase confirmStockDeductedUseCase,
            EventPayloadConverter eventPayloadConverter
    ) {
        return envelope -> {
            StockDeductedKafkaEvent event = eventPayloadConverter.convert(envelope.payload(), StockDeductedKafkaEvent.class);
            log.info("Received StockDeductedEvent: orderId={}, productId={}", event.orderId(), event.productId());
            confirmStockDeductedUseCase.execute(new ConfirmStockDeductedCommand(event.orderId()));
        };
    }

    @Bean
    public Consumer<EventEnvelope<JsonNode>> onStockDeductionFailed(
            CancelOrderByStockFailureUseCase cancelOrderByStockFailureUseCase,
            EventPayloadConverter eventPayloadConverter
    ) {
        return envelope -> {
            StockDeductionFailedKafkaEvent event = eventPayloadConverter.convert(envelope.payload(), StockDeductionFailedKafkaEvent.class);
            log.info("Received StockDeductionFailedEvent: orderId={}, productId={}, errorCode={}",
                    event.orderId(), event.productId(), event.errorCode());
            cancelOrderByStockFailureUseCase.execute(new CancelOrderByStockFailureCommand(event.orderId()));
        };
    }
}
