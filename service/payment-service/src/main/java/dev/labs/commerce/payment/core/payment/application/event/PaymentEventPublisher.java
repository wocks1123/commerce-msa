package dev.labs.commerce.payment.core.payment.application.event;

public interface PaymentEventPublisher {

    void publishPaymentInitialized(PaymentInitializedEvent event);

    void publishPaymentApproved(PaymentApprovedEvent event);

    void publishPaymentFailed(PaymentFailedEvent event);

}
