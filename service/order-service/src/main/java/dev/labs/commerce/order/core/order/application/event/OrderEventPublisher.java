package dev.labs.commerce.order.core.order.application.event;

public interface OrderEventPublisher {

    void publishOrderAborted(OrderAbortedEvent event);

    void publishOrderExpired(OrderExpiredEvent event);

    void publishOrderPaid(OrderPaidEvent event);

}
