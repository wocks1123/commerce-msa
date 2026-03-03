package dev.labs.commerce.order.core.order.application.event;

import dev.labs.commerce.order.core.order.domain.event.OrderAbortedEvent;
import dev.labs.commerce.order.core.order.domain.event.OrderCreatedEvent;

public interface OrderEventPublisher {

    void publishOrderCreated(OrderCreatedEvent event);

    void publishOrderAborted(OrderAbortedEvent event);

}
