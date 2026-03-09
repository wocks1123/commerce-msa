package dev.labs.commerce.order.core.order.application.event;

import dev.labs.commerce.order.core.order.application.event.OrderAbortedEvent;
import dev.labs.commerce.order.core.order.application.event.OrderCreatedEvent;

public interface OrderEventPublisher {

    void publishOrderCreated(OrderCreatedEvent event);

    void publishOrderAborted(OrderAbortedEvent event);

}
