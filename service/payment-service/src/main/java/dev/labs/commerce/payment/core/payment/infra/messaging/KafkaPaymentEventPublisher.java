package dev.labs.commerce.payment.core.payment.infra.messaging;

import dev.labs.commerce.common.event.EventEnvelope;
import dev.labs.commerce.common.event.EventPublisher;
import dev.labs.commerce.payment.core.payment.application.event.PaymentEventPublisher;
import dev.labs.commerce.payment.core.payment.application.event.PaymentInitializedEvent;
import dev.labs.commerce.payment.core.payment.application.event.PaymentApprovedEvent;
import dev.labs.commerce.payment.core.payment.application.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class KafkaPaymentEventPublisher implements PaymentEventPublisher {

    private static final String PAYMENT_INITIALIZED_BINDING = "payment-initialized-out-0";
    private static final String PAYMENT_APPROVED_BINDING = "payment-approved-out-0";
    private static final String PAYMENT_FAILED_BINDING = "payment-failed-out-0";

    private final EventPublisher eventPublisher;

    @Override
    public void publishPaymentInitialized(PaymentInitializedEvent event) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publish(
                        PAYMENT_INITIALIZED_BINDING,
                        event.orderId(),
                        EventEnvelope.of(event, PaymentInitializedEvent.class)
                );
            }
        });
    }

    @Override
    public void publishPaymentApproved(PaymentApprovedEvent event) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publish(
                        PAYMENT_APPROVED_BINDING,
                        event.paymentId(),
                        EventEnvelope.of(event, PaymentApprovedEvent.class)
                );
            }
        });
    }

    @Override
    public void publishPaymentFailed(PaymentFailedEvent event) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publish(
                        PAYMENT_FAILED_BINDING,
                        event.paymentId(),
                        EventEnvelope.of(event, PaymentFailedEvent.class)
                );
            }
        });
    }
}
