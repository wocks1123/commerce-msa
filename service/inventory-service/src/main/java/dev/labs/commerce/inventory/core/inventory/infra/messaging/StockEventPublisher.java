package dev.labs.commerce.inventory.core.inventory.infra.messaging;

import dev.labs.commerce.common.event.EventEnvelope;
import dev.labs.commerce.common.event.EventPublisher;
import dev.labs.commerce.inventory.core.inventory.application.event.StockDeductedEvent;
import dev.labs.commerce.inventory.core.inventory.application.event.StockDeductionFailedEvent;
import dev.labs.commerce.inventory.core.inventory.infra.messaging.dto.StockDeductedKafkaEvent;
import dev.labs.commerce.inventory.core.inventory.infra.messaging.dto.StockDeductionFailedKafkaEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class StockEventPublisher {

    private static final String TOPIC_STOCK_DEDUCTED = "stock.deducted";
    private static final String TOPIC_STOCK_DEDUCTION_FAILED = "stock.deduction-failed";

    private final EventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStockDeducted(StockDeductedEvent event) {
        var payload = new StockDeductedKafkaEvent(
                event.productId(),
                event.orderId(),
                event.quantity(),
                event.remainingQuantity()
        );
        eventPublisher.publish(TOPIC_STOCK_DEDUCTED, event.orderId(),
                EventEnvelope.of(payload, StockDeductedKafkaEvent.class));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void onStockDeductionFailed(StockDeductionFailedEvent event) {
        var payload = new StockDeductionFailedKafkaEvent(
                event.productId(),
                event.orderId(),
                event.quantity(),
                event.errorCode()
        );
        eventPublisher.publish(TOPIC_STOCK_DEDUCTION_FAILED, event.orderId(),
                EventEnvelope.of(payload, StockDeductionFailedKafkaEvent.class));
    }
}
