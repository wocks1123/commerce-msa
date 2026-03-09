package dev.labs.commerce.inventory.core.inventory.infra.messaging;

import dev.labs.commerce.common.event.EventEnvelope;
import dev.labs.commerce.common.event.EventPublisher;
import dev.labs.commerce.inventory.core.inventory.application.event.StockDeductedEvent;
import dev.labs.commerce.inventory.core.inventory.application.event.StockDeductionFailedEvent;
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
        eventPublisher.publish(TOPIC_STOCK_DEDUCTED, event.orderId(),
                EventEnvelope.of(event, StockDeductedEvent.class));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void onStockDeductionFailed(StockDeductionFailedEvent event) {
        eventPublisher.publish(TOPIC_STOCK_DEDUCTION_FAILED, event.orderId(),
                EventEnvelope.of(event, StockDeductionFailedEvent.class));
    }
}
