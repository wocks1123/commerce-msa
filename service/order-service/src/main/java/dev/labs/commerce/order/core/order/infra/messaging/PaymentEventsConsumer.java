package dev.labs.commerce.order.core.order.infra.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import dev.labs.commerce.common.event.EventEnvelope;
import dev.labs.commerce.common.event.EventPayloadConverter;
import dev.labs.commerce.order.core.order.application.usecase.AbortOrderByPaymentFailureUseCase;
import dev.labs.commerce.order.core.order.application.usecase.ConfirmPaidUseCase;
import dev.labs.commerce.order.core.order.application.usecase.dto.AbortOrderByPaymentFailureCommand;
import dev.labs.commerce.order.core.order.application.usecase.dto.ConfirmPaidCommand;
import dev.labs.commerce.order.core.order.infra.messaging.dto.PaymentApprovedKafkaEvent;
import dev.labs.commerce.order.core.order.infra.messaging.dto.PaymentFailedKafkaEvent;
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
            PaymentApprovedKafkaEvent event = eventPayloadConverter.convert(envelope.payload(), PaymentApprovedKafkaEvent.class);
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
            PaymentFailedKafkaEvent event = eventPayloadConverter.convert(envelope.payload(), PaymentFailedKafkaEvent.class);
            log.info("Received PaymentFailedEvent: orderId={}, errorCode={}", event.orderId(), event.errorCode());
            abortOrderByPaymentFailureUseCase.execute(new AbortOrderByPaymentFailureCommand(event.orderId()));
        };
    }
}
