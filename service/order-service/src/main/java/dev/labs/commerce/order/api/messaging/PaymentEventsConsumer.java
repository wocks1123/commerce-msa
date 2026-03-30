package dev.labs.commerce.order.api.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import dev.labs.commerce.common.event.EventEnvelope;
import dev.labs.commerce.common.event.EventPayloadConverter;
import dev.labs.commerce.order.api.messaging.dto.PaymentApprovedEvent;
import dev.labs.commerce.order.api.messaging.dto.PaymentFailedEvent;
import dev.labs.commerce.order.core.order.application.usecase.AbortOrderByPaymentFailureUseCase;
import dev.labs.commerce.order.core.order.application.usecase.ConfirmPaidUseCase;
import dev.labs.commerce.order.core.order.application.usecase.dto.AbortOrderByPaymentFailureCommand;
import dev.labs.commerce.order.core.order.application.usecase.dto.ConfirmPaidCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class PaymentEventsConsumer {

    @Bean
    public Consumer<EventEnvelope<JsonNode>> onPaymentApproved(
            ConfirmPaidUseCase confirmPaidUseCase,
            EventPayloadConverter eventPayloadConverter
    ) {
        return envelope -> {
            PaymentApprovedEvent event = eventPayloadConverter.convert(envelope.payload(), PaymentApprovedEvent.class);
            log.info("Received PaymentApprovedEvent: orderId={}, amount={}", event.orderId(), event.amount());
            confirmPaidUseCase.execute(new ConfirmPaidCommand(
                    event.orderId(),
                    event.customerId(),
                    event.amount(),
                    event.currency()
            ));
        };
    }

    @Bean
    public Consumer<EventEnvelope<JsonNode>> onPaymentFailed(
            AbortOrderByPaymentFailureUseCase abortOrderByPaymentFailureUseCase,
            EventPayloadConverter eventPayloadConverter
    ) {
        return envelope -> {
            PaymentFailedEvent event = eventPayloadConverter.convert(envelope.payload(), PaymentFailedEvent.class);
            log.info("Received PaymentFailedEvent: orderId={}, failureCode={}", event.orderId(), event.failureCode());
            abortOrderByPaymentFailureUseCase.execute(new AbortOrderByPaymentFailureCommand(event.orderId()));
        };
    }
}
