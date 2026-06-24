package dev.labs.commerce.payment.core.payment.infra.messaging;

import dev.labs.commerce.payment.core.payment.application.event.PaymentApprovedEvent;
import dev.labs.commerce.payment.core.payment.application.event.PaymentEventPublisher;
import dev.labs.commerce.payment.core.payment.application.event.PaymentExpiredEvent;
import dev.labs.commerce.payment.core.payment.application.event.PaymentFailedEvent;
import dev.labs.commerce.payment.core.payment.application.event.PaymentInitializedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxPaymentEventPublisher implements PaymentEventPublisher {

    private static final String PAYMENT_INITIALIZED_BINDING = "payment-initialized-out-0";
    private static final String PAYMENT_APPROVED_BINDING = "payment-approved-out-0";
    private static final String PAYMENT_FAILED_BINDING = "payment-failed-out-0";
    private static final String PAYMENT_EXPIRED_BINDING = "payment-expired-out-0";

    private final OutboxAppender outboxAppender;

    @Override
    public void publishPaymentInitialized(PaymentInitializedEvent event) {
        outboxAppender.append(PAYMENT_INITIALIZED_BINDING, event.orderId(),
                PaymentInitializedEvent.class.getSimpleName(), event);
    }

    @Override
    public void publishPaymentApproved(PaymentApprovedEvent event) {
        outboxAppender.append(PAYMENT_APPROVED_BINDING, event.paymentId(),
                PaymentApprovedEvent.class.getSimpleName(), event);
    }

    @Override
    public void publishPaymentFailed(PaymentFailedEvent event) {
        outboxAppender.append(PAYMENT_FAILED_BINDING, event.paymentId(),
                PaymentFailedEvent.class.getSimpleName(), event);
    }

    @Override
    public void publishPaymentExpired(PaymentExpiredEvent event) {
        outboxAppender.append(PAYMENT_EXPIRED_BINDING, event.paymentId(),
                PaymentExpiredEvent.class.getSimpleName(), event);
    }
}
