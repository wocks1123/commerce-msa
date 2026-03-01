package dev.labs.commerce.payment.core.payment.infra.messaging;

import dev.labs.commerce.common.event.EventEnvelope;
import dev.labs.commerce.common.event.EventPublisher;
import dev.labs.commerce.payment.core.payment.application.event.PaymentEventPublisher;
import dev.labs.commerce.payment.core.payment.domain.event.PaymentApprovedEvent;
import dev.labs.commerce.payment.core.payment.domain.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaPaymentEventPublisher implements PaymentEventPublisher {

    private static final String PAYMENT_APPROVED_BINDING = "payment-approved-out-0";
    private static final String PAYMENT_FAILED_BINDING = "payment-failed-out-0";

    private final EventPublisher eventPublisher;

    @Override
    public void publishPaymentApproved(PaymentApprovedEvent event) {
        eventPublisher.publish(
                PAYMENT_APPROVED_BINDING,
                event.paymentId(),
                EventEnvelope.of(event, PaymentApprovedEvent.class)
        );
    }

    @Override
    public void publishPaymentFailed(PaymentFailedEvent event) {
        eventPublisher.publish(
                PAYMENT_FAILED_BINDING,
                event.paymentId(),
                EventEnvelope.of(event, PaymentFailedEvent.class)
        );
    }
}
