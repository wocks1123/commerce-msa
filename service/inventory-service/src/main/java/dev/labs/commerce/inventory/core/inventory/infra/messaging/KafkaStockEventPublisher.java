package dev.labs.commerce.inventory.core.inventory.infra.messaging;

import dev.labs.commerce.common.event.EventEnvelope;
import dev.labs.commerce.common.event.EventPublisher;
import dev.labs.commerce.inventory.core.inventory.application.event.StockDeductedEvent;
import dev.labs.commerce.inventory.core.inventory.application.event.StockDeductionFailedEvent;
import dev.labs.commerce.inventory.core.inventory.application.event.StockEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class KafkaStockEventPublisher implements StockEventPublisher {

    private static final String TOPIC_STOCK_DEDUCTED = "stock.deducted";
    private static final String TOPIC_STOCK_DEDUCTION_FAILED = "stock.deduction-failed";

    private final EventPublisher eventPublisher;

    @Override
    public void publishStockDeducted(StockDeductedEvent event) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publish(
                        TOPIC_STOCK_DEDUCTED,
                        event.orderId(),
                        EventEnvelope.of(event, StockDeductedEvent.class)
                );
            }
        });
    }

    @Override
    public void publishStockDeductionFailed(StockDeductionFailedEvent event) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    eventPublisher.publish(
                            TOPIC_STOCK_DEDUCTION_FAILED,
                            event.orderId(),
                            EventEnvelope.of(event, StockDeductionFailedEvent.class)
                    );
                }
            }
        });
    }
}
