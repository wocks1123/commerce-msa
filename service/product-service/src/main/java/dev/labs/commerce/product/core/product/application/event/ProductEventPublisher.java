package dev.labs.commerce.product.core.product.application.event;

public interface ProductEventPublisher {

    void publishProductRegistered(ProductRegisteredEvent event);

    void publishProductActivated(ProductActivatedEvent event);

    void publishProductDeactivated(ProductDeactivatedEvent event);

    void publishProductDiscontinued(ProductDiscontinuedEvent event);

    void publishProductModified(ProductModifiedEvent event);

}
