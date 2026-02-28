package dev.labs.commerce.product.core.product.infra.messaging;

import dev.labs.commerce.common.event.EventEnvelope;
import dev.labs.commerce.common.event.EventPublisher;
import dev.labs.commerce.product.core.product.application.event.ProductEventPublisher;
import dev.labs.commerce.product.core.product.domain.event.ProductRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaProductEventPublisher implements ProductEventPublisher {

    private static final String PRODUCT_REGISTERED_BINDING = "product-registered-out-0";

    private final EventPublisher eventPublisher;


    @Override
    public void publishProductRegistered(ProductRegisteredEvent event) {
        eventPublisher.publish(
                PRODUCT_REGISTERED_BINDING,
                event.productId().toString(),
                EventEnvelope.of(event, ProductRegisteredEvent.class)
        );
    }

}
