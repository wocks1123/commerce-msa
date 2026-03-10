package dev.labs.commerce.inventory.core.inventory.application.event;

public interface StockEventPublisher {

    void publishStockDeducted(StockDeductedEvent event);

    void publishStockDeductionFailed(StockDeductionFailedEvent event);

}
