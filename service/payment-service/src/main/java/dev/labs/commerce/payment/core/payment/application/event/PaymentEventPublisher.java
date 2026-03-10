package dev.labs.commerce.payment.core.payment.application.event;

import dev.labs.commerce.payment.core.payment.application.event.PaymentApprovedEvent;
import dev.labs.commerce.payment.core.payment.application.event.PaymentFailedEvent;

public interface PaymentEventPublisher {

    void publishPaymentApproved(PaymentApprovedEvent event);

    void publishPaymentFailed(PaymentFailedEvent event);

}
