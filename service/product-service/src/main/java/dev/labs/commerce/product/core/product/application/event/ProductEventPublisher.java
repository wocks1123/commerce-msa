package dev.labs.commerce.product.core.product.application.event;

import dev.labs.commerce.product.core.product.application.event.ProductRegisteredEvent;

public interface ProductEventPublisher {

    void publishProductRegistered(ProductRegisteredEvent event);

}
