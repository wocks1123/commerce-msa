package dev.labs.commerce.payment.core.payment.application.event;

import dev.labs.commerce.payment.core.payment.domain.event.PaymentApprovedEvent;

public interface PaymentEventPublisher {

    void publishPaymentApproved(PaymentApprovedEvent event);
}
