package dev.labs.commerce.inventory.core.inventory.infra.messaging;

import dev.labs.commerce.common.event.EventEnvelope;
import dev.labs.commerce.common.event.EventPublisher;
import dev.labs.commerce.inventory.core.inventory.application.event.StockEventPublisher;
import dev.labs.commerce.inventory.core.inventory.application.event.StockReservationFailedEvent;
import dev.labs.commerce.inventory.core.inventory.application.event.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class KafkaStockEventPublisher implements StockEventPublisher {

    private static final String TOPIC_STOCK_RESERVED = "stock.reserved";
    private static final String TOPIC_STOCK_RESERVATION_FAILED = "stock.reservation-failed";

    private final EventPublisher eventPublisher;

    @Override
    public void publishStockReserved(StockReservedEvent event) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publish(
                        TOPIC_STOCK_RESERVED,
                        event.orderId(),
                        EventEnvelope.of(event, StockReservedEvent.class)
                );
            }
        });
    }

    @Override
    public void publishStockReservationFailed(StockReservationFailedEvent event) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    eventPublisher.publish(
                            TOPIC_STOCK_RESERVATION_FAILED,
                            event.orderId(),
                            EventEnvelope.of(event, StockReservationFailedEvent.class)
                    );
                }
            }
        });
    }
}
