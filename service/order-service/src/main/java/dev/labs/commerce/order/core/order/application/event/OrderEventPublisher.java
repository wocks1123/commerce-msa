package dev.labs.commerce.order.core.order.application.event;

public interface OrderEventPublisher {

    void publishOrderCreated(OrderCreatedEvent event);

    void publishOrderAborted(OrderAbortedEvent event);

    void publishOrderPaid(OrderPaidEvent event);

}
