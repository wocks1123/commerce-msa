package dev.labs.commerce.order.core.order.infra.messaging;

import dev.labs.commerce.order.core.order.application.event.OrderAbortedEvent;
import dev.labs.commerce.order.core.order.application.event.OrderEventPublisher;
import dev.labs.commerce.order.core.order.application.event.OrderExpiredEvent;
import dev.labs.commerce.order.core.order.application.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
@RequiredArgsConstructor
public class OutboxOrderEventPublisher implements OrderEventPublisher {

    private static final String ORDER_ABORTED_BINDING = "order-aborted-out-0";
    private static final String ORDER_EXPIRED_BINDING = "order-expired-out-0";
    private static final String ORDER_PAID_BINDING = "order-paid-out-0";

    private final OutboxAppender outboxAppender;

    @Override
    public void publishOrderAborted(OrderAbortedEvent event) {
        outboxAppender.append(ORDER_ABORTED_BINDING, event.orderId(),
                OrderAbortedEvent.class.getSimpleName(), event);
    }

    @Override
    public void publishOrderExpired(OrderExpiredEvent event) {
        outboxAppender.append(ORDER_EXPIRED_BINDING, event.orderId(),
                OrderExpiredEvent.class.getSimpleName(), event);
    }

    @Override
    public void publishOrderPaid(OrderPaidEvent event) {
        outboxAppender.append(ORDER_PAID_BINDING, event.orderId(),
                OrderPaidEvent.class.getSimpleName(), event);
    }

}
