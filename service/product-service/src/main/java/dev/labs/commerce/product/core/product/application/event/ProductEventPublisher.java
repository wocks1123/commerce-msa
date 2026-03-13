package dev.labs.commerce.product.core.product.application.event;

public interface ProductEventPublisher {

    void publishProductRegistered(ProductRegisteredEvent event);

}
