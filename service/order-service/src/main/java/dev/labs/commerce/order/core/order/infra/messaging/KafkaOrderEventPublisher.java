package dev.labs.commerce.order.core.order.infra.messaging;

import dev.labs.commerce.common.event.EventEnvelope;
import dev.labs.commerce.common.event.EventPublisher;
import dev.labs.commerce.order.core.order.application.event.OrderAbortedEvent;
import dev.labs.commerce.order.core.order.application.event.OrderEventPublisher;
import dev.labs.commerce.order.core.order.application.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class KafkaOrderEventPublisher implements OrderEventPublisher {

    private static final String ORDER_ABORTED_BINDING = "order-aborted-out-0";
    private static final String ORDER_PAID_BINDING = "order-paid-out-0";

    private final EventPublisher eventPublisher;

    @Override
    public void publishOrderAborted(OrderAbortedEvent event) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publish(
                        ORDER_ABORTED_BINDING,
                        event.orderId(),
                        EventEnvelope.of(event, OrderAbortedEvent.class)
                );
            }
        });
    }

    @Override
    public void publishOrderPaid(OrderPaidEvent event) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publish(
                        ORDER_PAID_BINDING,
                        event.orderId(),
                        EventEnvelope.of(event, OrderPaidEvent.class)
                );
            }
        });
    }

}
