package dev.labs.commerce.inventory.core.inventory.application.event;

public interface StockEventPublisher {

    void publishStockReserved(StockReservedEvent event);

    void publishStockReservationFailed(StockReservationFailedEvent event);

}
