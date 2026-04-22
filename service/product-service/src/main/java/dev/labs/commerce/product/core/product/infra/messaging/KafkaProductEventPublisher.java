package dev.labs.commerce.product.core.product.infra.messaging;

import dev.labs.commerce.common.event.EventEnvelope;
import dev.labs.commerce.common.event.EventPublisher;
import dev.labs.commerce.product.core.product.application.event.ProductActivatedEvent;
import dev.labs.commerce.product.core.product.application.event.ProductDeactivatedEvent;
import dev.labs.commerce.product.core.product.application.event.ProductDiscontinuedEvent;
import dev.labs.commerce.product.core.product.application.event.ProductEventPublisher;
import dev.labs.commerce.product.core.product.application.event.ProductModifiedEvent;
import dev.labs.commerce.product.core.product.application.event.ProductRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class KafkaProductEventPublisher implements ProductEventPublisher {

    private static final String PRODUCT_REGISTERED_BINDING = "product-registered-out-0";
    private static final String PRODUCT_ACTIVATED_BINDING = "product-activated-out-0";
    private static final String PRODUCT_DEACTIVATED_BINDING = "product-deactivated-out-0";
    private static final String PRODUCT_DISCONTINUED_BINDING = "product-discontinued-out-0";
    private static final String PRODUCT_MODIFIED_BINDING = "product-modified-out-0";

    private final EventPublisher eventPublisher;

    @Override
    public void publishProductRegistered(ProductRegisteredEvent event) {
        publishAfterCommit(PRODUCT_REGISTERED_BINDING, event.productId(),
                EventEnvelope.of(event, ProductRegisteredEvent.class));
    }

    @Override
    public void publishProductActivated(ProductActivatedEvent event) {
        publishAfterCommit(PRODUCT_ACTIVATED_BINDING, event.productId(),
                EventEnvelope.of(event, ProductActivatedEvent.class));
    }

    @Override
    public void publishProductDeactivated(ProductDeactivatedEvent event) {
        publishAfterCommit(PRODUCT_DEACTIVATED_BINDING, event.productId(),
                EventEnvelope.of(event, ProductDeactivatedEvent.class));
    }

    @Override
    public void publishProductDiscontinued(ProductDiscontinuedEvent event) {
        publishAfterCommit(PRODUCT_DISCONTINUED_BINDING, event.productId(),
                EventEnvelope.of(event, ProductDiscontinuedEvent.class));
    }

    @Override
    public void publishProductModified(ProductModifiedEvent event) {
        publishAfterCommit(PRODUCT_MODIFIED_BINDING, event.productId(),
                EventEnvelope.of(event, ProductModifiedEvent.class));
    }

    private void publishAfterCommit(String binding, Long productId, EventEnvelope<?> envelope) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publish(binding, productId.toString(), envelope);
            }
        });
    }

}
