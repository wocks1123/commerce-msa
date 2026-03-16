package dev.labs.commerce.inventory.core.inventory.application.event;

public interface StockEventPublisher {

    void publishStockReservationFailed(StockReservationFailedEvent event);

}
