package dev.labs.commerce.product.core.product.infra.messaging;

import dev.labs.commerce.common.event.EventEnvelope;
import dev.labs.commerce.common.event.EventPublisher;
import dev.labs.commerce.product.core.product.application.event.ProductEventPublisher;
import dev.labs.commerce.product.core.product.application.event.ProductRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class KafkaProductEventPublisher implements ProductEventPublisher {

    private static final String PRODUCT_REGISTERED_BINDING = "product-registered-out-0";

    private final EventPublisher eventPublisher;

    @Override
    public void publishProductRegistered(ProductRegisteredEvent event) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publish(
                        PRODUCT_REGISTERED_BINDING,
                        event.productId().toString(),
                        EventEnvelope.of(event, ProductRegisteredEvent.class)
                );
            }
        });
    }

}
